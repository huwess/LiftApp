package com.example.liftapp.bottom_nav.fab_add

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.liftapp.databinding.ActivityExerciseBinding
import androidx.activity.viewModels
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.liftapp.R
import com.example.liftapp.helper.audio.TextToSpeechHelper
import com.example.liftapp.helper.calculator.Calculator
import com.example.liftapp.helper.record.StrengthRecordHelper
import com.example.liftapp.helper.users.UserProfileHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.Timer
import java.util.TimerTask
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
    private lateinit var ttsHelper: TextToSpeechHelper
    private lateinit var userProfileHelper: UserProfileHelper
    private lateinit var strengthRecordHelper: StrengthRecordHelper
    private lateinit var calculator: Calculator
    private lateinit var firebaseAuth: FirebaseAuth
    private var isPoseDetectionActive = false

    private lateinit var repCountTextView: TextView
    private lateinit var stageTextView: TextView
    private lateinit var signTextView: TextView
    private var weight: Double = 0.0
    private var timer: CountDownTimer? = null
    private var exerciseStartTime: Long = 0L
    private var exerciseTimer: Timer? = null
    private var lastSpeechTime = 0L
    private val SPEECH_COOLDOWN = 3000 // 3 seconds between announcements
    private var currentSign = ""
    private var isSpeechPending = false
    private val handler = Handler(Looper.getMainLooper())



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
        ttsHelper = TextToSpeechHelper(this)
        userProfileHelper = UserProfileHelper()
        strengthRecordHelper = StrengthRecordHelper()
        calculator = Calculator()
        firebaseAuth = FirebaseAuth.getInstance()

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

        startCountdownTimer()




        binding.stopButton.setOnClickListener {
            ttsHelper.speakText("Exercise Cancelled.")
            finish()
        }

        binding.finishButton.setOnClickListener {
            calculateAndSaveRecord(weight)
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

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onRepsUpdated(reps: Int) {
        repCountTextView.text = String.format("%d", reps)

        binding.finishButton.isEnabled = reps > 0
    }

    override fun onStageUpdated(stage: String) {
        stageTextView.text = stage
    }

    override fun onSignUpdated(sign: String) {
        if (sign != currentSign && sign != "Proper") {
            currentSign = sign
            handler.removeCallbacksAndMessages(null)
            isSpeechPending = false

            val timeSinceLastSpeech = System.currentTimeMillis() - lastSpeechTime

            if (timeSinceLastSpeech > SPEECH_COOLDOWN) {
                triggerSpeech(sign)
            } else {
                isSpeechPending = true
                handler.postDelayed({
                    if (isSpeechPending) {
                        triggerSpeech(sign)
                    }
                }, SPEECH_COOLDOWN - timeSinceLastSpeech)
            }
        }
        signTextView.text = sign
    }
    private fun triggerSpeech(sign: String) {
        ttsHelper.stopSpeaking() // Takes advantage of existing stopSpeaking()
        ttsHelper.speakText(sign)
        lastSpeechTime = System.currentTimeMillis()
        isSpeechPending = false
    }



    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraFacing)
            .build()

        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY // Automatically picks the best resolution
            )
            .build()

        preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector) // Use new API
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector) // Apply to image analysis too
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    detectPose(image)
                }
            }

        // Unbind existing use cases
        cameraProvider.unbindAll()

        try {
            // Bind the camera use cases
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }


    private fun detectPose(imageProxy: ImageProxy) {
        if (isPoseDetectionActive && this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        } else {
            imageProxy.close() // Release the image if detection is not active
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

        // Indices of the landmarks we want to extract
//        val landmarkIndices = listOf(23, 11, 13, 24, 12, 14, 15, 16, 21, 22)
//        val coordinates = getLandmarkCoordinates(resultBundle, landmarkIndices)

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

    private fun startCountdownTimer() {
        binding.countdownTimer.visibility = View.VISIBLE
        ttsHelper.speakText("Starting in five seconds")
        val timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.countdownTimer.text = "$secondsRemaining s"
            }

            override fun onFinish() {
                binding.countdownTimer.visibility = View.GONE
                binding.finishButton.visibility = View.VISIBLE
                binding.timerLayout.visibility = View.VISIBLE
                isPoseDetectionActive = true



                // Initialize PoseLandmarkerHelper only after countdown
                backgroundExecutor.execute {
                    poseLandmarkerHelper = PoseLandmarkerHelper(
                        context = this@ExerciseActivity,
                        runningMode = RunningMode.LIVE_STREAM,
                        minPoseDetectionConfidence = PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE,
                        minPoseTrackingConfidence = PoseLandmarkerHelper.DEFAULT_POSE_TRACKING_CONFIDENCE,
                        minPosePresenceConfidence = PoseLandmarkerHelper.DEFAULT_POSE_PRESENCE_CONFIDENCE,
                        currentDelegate = PoseLandmarkerHelper.DELEGATE_CPU,
                        poseLandmarkerHelperListener = this@ExerciseActivity
                    )
                }

                startExerciseTimer()
            }
        }.start()
    }

    private fun calculateAndSaveRecord(weight : Double) {
        val duration = stopExerciseTimer()
        firebaseAuth.currentUser?.let {
            userProfileHelper.fetchUserData(it.uid) { user ->
                if(user != null) {
                    val userWeight = user.weight
                    val userAge = user.age
                    val userGender = user.gender
                    val unit = user.unit

                    val reps_final = repCountTextView.text.toString().toInt()
                    val oneRepMax = calculator.oneRepMaxCalculator(weight, reps_final)

                    val strengthLevel = calculator.assStrengthLvl(userAge, userWeight, unit, oneRepMax, userGender)
                    strengthRecordHelper.saveStrengthRecord(
                        reps_final,
                        weight,
                        oneRepMax,
                        strengthLevel,
                        duration,
                        onSuccess = {
                            ttsHelper.speakText("Exercise Completed")
                            finish()
                        },
                        onFailure = { error ->
                            ttsHelper.speakText("Failed to save exercise record.")
                            error.printStackTrace()
                        }
                    )
                } else {
                    Log.e("ExerciseActivity", "User data is null")
                }
            }
        }

    }
    private fun startExerciseTimer() {
        exerciseStartTime = System.currentTimeMillis()
        exerciseTimer = Timer()
        exerciseTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val elapsedMillis = System.currentTimeMillis() - exerciseStartTime
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
                    binding.timerTV.text = String.format("%02d:%02d", minutes, seconds)
                }
            }
        }, 0, 1000) // Update every second
    }

    private fun stopExerciseTimer(): Long {
        exerciseTimer?.cancel()
        return System.currentTimeMillis() - exerciseStartTime
    }

}