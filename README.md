# AutoCare – Vehicle Service & Maintenance Tracker


AutoCare is an Android mobile app built to help vehicle owners track maintenance records, service history, and expenses effortlessly. Developed with Kotlin, XML, Firebase, and Cloudinary, it offers a seamless way to manage multiple vehicles and generate service reports.

## 🔧 Key Features

### ✅ Vehicle Management
- Add, edit, or delete multiple vehicles
- Track brand, model, registration number, and mileage
- View all vehicles in a centralized dashboard

### 🔧 Service Tracking
- Log service details (date, odometer reading, service type)
- Dynamic service checklists (5000km/40000km/100000km services)
- Upload service bills via camera/gallery (Cloudinary integration)
- Calculate and track service costs

### 📊 Dashboard & Reports
- Visualize vehicle service histories
- Generate and download PDF reports
- Filter records by date range
- Delete outdated service entries

### 🔐 User Authentication
- Secure login/signup with Firebase Auth
- Password reset functionality
- Account deletion option

## 🛠️ Technologies Used

| Category        | Technologies                          |
|-----------------|---------------------------------------|
| Frontend        | Kotlin, XML (Android Studio)          |
| Backend         | Firebase Authentication, Realtime DB  |
| Cloud Storage   | Cloudinary (image uploads)            |
| PDF Generation  | Android PDF libraries                 |
| Dependency Injection | Hilt                              |

## 📥 Installation

1. Clone the repository:
```bash
git clone https://github.com/Hasith819/AutoCare-Android-App.git
