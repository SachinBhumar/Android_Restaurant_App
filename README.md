# RestaurantApp

An Android application for Browse diverse restaurant cuisines and dishes, managing a cart, and simulating order placement.

## Table of Contents

-   [About the Project](#about-the-project)
-   [Features](#features)
-   [Technologies Used](#technologies-used)
-   [Setup and Installation](#setup-and-installation)
-   [Usage](#usage)
-   [Screenshots](#screenshots)
-   [Contact](#contact)
-   [License](#license)

## About the Project

This Android Restaurant App allows users to browse a variety of food items grouped by cuisine categories. Users can view details of individual dishes, add them to a cart, adjust quantities, and then proceed to place an order. For demonstration purposes, the "Place Order" functionality now simulates a successful order placement, displaying an "Order Successful!" message.

The app was developed with the original intent for seamless integration with backend APIs for fetching food items, retrieving item details, filtering, and processing payments.

## Features

* **Diverse Cuisine Browse:** Explore food items categorized by different cuisines (e.g., North Indian, Chinese, Mexican).
* **Interactive Home Screen:** Features horizontal infinite scroll for cuisine categories and displays top 3 famous dishes with image, price, and rating.
* **Detailed Dish View:** View specific dishes within each cuisine category.
* **Cart Management:** Add multiple quantities of the same dish to your cart.
* **Dynamic Total Calculation:** Cart screen calculates net total, CGST (2.5%), SGST (2.5%), and grand total.
* **Order Placement Simulation:** The "Place Order" button currently simulates a successful order, clearing the cart and displaying a success dialog, bypassing actual API calls for demonstration.
* **Multi-language Support:** (Remove this line if you haven't implemented language switching) Language selection button for switching between Hindi and English.

## Technologies Used

* **Android SDK**
* **Java**
* **XML** for UI Layouts
* **HTTP/S connections** (for API interaction)
* **JSON** parsing (for API responses)
* **Git** for version control

## Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/RestaurantApp.git](https://github.com/your-username/RestaurantApp.git)
    ```
    (Replace `your-username` with your actual GitHub username and `RestaurantApp` with your repository name).
2.  **Open in Android Studio:**
    * Launch Android Studio.
    * Select `Open an existing Android Studio project`.
    * Navigate to the cloned `RestaurantApp` directory and select it.
3.  **Sync Gradle:**
    * Let Android Studio sync the Gradle project. If prompted, update Gradle to the latest version.
4.  **Build and Run:**
    * Connect an Android device or start an AVD (Android Virtual Device).
    * Click the `Run` button (green play icon) in Android Studio to build and install the app on your device/emulator.

## Usage

1.  Browse cuisine categories by swiping horizontally on the home screen.
2.  Tap on a cuisine card to view dishes specific to that cuisine.
3.  From the home screen or cuisine specific screen, tap the "Add" button to add dishes to your cart.
4.  Access the cart screen by tapping the cart icon in the top right corner.
5.  In the cart, review your order and tap "Place Order" to see the order success simulation.

## Screenshots

*(This section is crucial for a "nice" README!)*

To add screenshots:
1.  Create a folder named `screenshots` (or `images`) in the root of your project on your local machine.
2.  Place your app screenshots (e.g., `home_screen.png`, `cart_screen.png`, `order_success.png`) into this folder.
3.  Commit and push this `screenshots` folder along with your code to GitHub.
4.  Then, update the paths below to link to them.

Example links (replace with your actual image names):

![Home Screen](screenshots/home_screen.png)
![Cart Screen](screenshots/cart_screen.png)
![Order Success Dialog](screenshots/order_success.png)

## Contact

* Your Name - [Your Email](mailto:youremail@example.com)
* Your GitHub Profile - [https://github.com/your-username](https://github.com/your-username)
* LinkedIn Profile (Optional) - [https://www.linkedin.com/in/yourprofile/](https://www.linkedin.com/in/yourprofile/)

## License

Distributed under the MIT License. See `LICENSE` for more information.
*(If you want to include a license, create a new file named `LICENSE` in your project's root directory and paste the MIT License text into it. You can find the MIT License text easily online.)*
