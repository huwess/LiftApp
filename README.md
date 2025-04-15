# 🏋️ LiftApp

**LiftApp** is an Android fitness application designed to help users improve their strength training form and track workout progress using real-time pose detection. Built with MediaPipe, Firebase, and MPAndroidChart, the app gives live feedback, rep counting, and strength assessment for upper body workouts.

---

## 📱 Features

- **Real-time Pose Detection**  
  Utilizes MediaPipe's PoseLandmarker to track body movements during workouts.
  
- **Form Feedback**  
  Calculates joint angles and Z-axis depth to provide live analysis of user posture and exercise execution.

- **Rep Counting**  
  Automatically detects and counts repetitions based on pose transitions and movement stages.

- **Strength Progression Tracking**  
  Tracks workout records per day and evaluates user strength level:
  - Elite = 5  
  - Advanced = 4  
  - Intermediate = 3  
  - Novice = 2  
  - Untrained = 1  
  - Unknown = 0

- **User Profile Management**  
  Save user information (name, age, weight, gender, unit) for personalized tracking.

- **Visual Charts**  
  Display strength records and progress using MPAndroidChart.

---



---

## ⚙️ Tech Stack

- **Android (Kotlin)**
- **Firebase Authentication** (User login/signup)
- **Firebase Realtime Database** (User and workout data)
- **MediaPipe PoseLandmarker** (Pose tracking)
- **MPAndroidChart** (Data visualization)

---

## 🚧 Coming Soon

- Lower body workout support  
- Video recording and playback for form review  
- Strength level history and achievements  
- Social features (sharing, challenges)

---

## 🧑‍💻 Team

LiftApp is developed by a team of three Computer Science students passionate about fitness and technology.
