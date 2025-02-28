package com.example.liftapp.bottom_nav.fab_add

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.liftapp.databinding.ActivityExerciseBinding
import androidx.activity.viewModels
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.liftapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ExerciseActivity : AppCompatActivity(), PoseLandmarkerHelper.LandmarkerListener, OverlayUpdateListener {

    companion object {
        private const val TAG = "Pose Landmarker"
    }

    private lateinit var binding: ActivityExerciseBinding
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private val viewModel: PoseLandmarkerViewModel by viewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private lateinit var repCountTextView: TextView
    private lateinit var stageTextView: TextView
    private lateinit var signTextView: TextView
    private var weight: Double = 0.0


    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()

        if (!CameraPermissionsFragment.hasPermissions(this)) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, CameraPermissionsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onPause() {
        super.onPause()

        if(this::poseLandmarkerHelper.isInitialized) {
            viewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence)
            viewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseTrackingConfidence)
            viewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPosePresenceConfidence)
            viewModel.setDelegate(poseLandmarkerHelper.currentDelegate)

            // Close the PoseLandmarkerHelper and release resources
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repCountTextView = binding.cameraContainer.findViewById(R.id.repition_count)
        stageTextView = binding.cameraContainer.findViewById(R.id.stage)
        signTextView = binding.cameraContainer.findViewById(R.id.sign)


        // Retrieve the passed weight value
        weight = intent.getDoubleExtra("DUMBBELL_WEIGHT", 0.0)


        val overlayView: OverlayView = binding.cameraContainer.findViewById(R.id.overlay)
        overlayView.overlayUpdateListener = this

        backgroundExecutor = Executors.newSingleThreadExecutor()

        binding.viewFinder.post {
            setUpCamera()
        }

        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = this,
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE,
                minPoseTrackingConfidence = PoseLandmarkerHelper.DEFAULT_POSE_TRACKING_CONFIDENCE,
                minPosePresenceConfidence = PoseLandmarkerHelper.DEFAULT_POSE_PRESENCE_CONFIDENCE,
                currentDelegate = PoseLandmarkerHelper.DELEGATE_CPU,
                poseLandmarkerHelperListener = this
            )
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(this)
        )
    }

    override fun onRepsUpdated(reps: Int) {
        repCountTextView.text = reps.toString()
    }

    override fun onStageUpdated(stage: String) {
        stageTextView.text = stage
    }

    override fun onSignUpdated(sign: String) {
        signTextView.text = sign
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectPose(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if(this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }

    private fun getLandmarkCoordinates(
        resultBundle: PoseLandmarkerHelper.ResultBundle,
        indices: List<Int>
    ): Map<String, Pair<Float, Float>> {
        val poseLandmarks = resultBundle.results.firstOrNull()?.landmarks()?.firstOrNull() ?: return emptyMap()
        val coordinates = mutableMapOf<String, Pair<Float, Float>>()

        indices.forEach { index ->
            if (index in poseLandmarks.indices) {
                val landmark = poseLandmarks[index]
                coordinates["Landmark $index"] = Pair(landmark.x(), landmark.y())
            }
        }
        return coordinates
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            binding.viewFinder.display.rotation
    }

    override fun onError(error: String, errorCode: Int) {

    }

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        if (binding != null) {

            // Indices of the landmarks we want to extract
            val landmarkIndices = listOf(23, 11, 13, 24, 12, 14, 15, 16, 21, 22)
            val coordinates = getLandmarkCoordinates(resultBundle, landmarkIndices)

//            // Log the coordinates for debugging
//            coordinates.forEach { (name, coord) ->
//                Log.d("Landmark", "$name: x=${coord.first}, y=${coord.second}")
//            }
//
//            // Log the coordinates for debugging
//            coordinates.forEach { (name, coord) ->
//                Log.d("Landmark", "$name: x=${coord.first}, y=${coord.second}")
//            }

            binding.overlay.setResults(
                resultBundle.results.first(),
                resultBundle.inputImageHeight,
                resultBundle.inputImageWidth,
                RunningMode.LIVE_STREAM
            )

            binding.overlay.invalidate()

        }

    }
}