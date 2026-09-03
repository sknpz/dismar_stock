# Dismar Stock

<p align="center">
  <img src="logo_dismar_PNG.png" alt="Dismar Stock Logo" width="220">
</p>

<p align="center">
  A lightweight inventory management web application built with HTML, CSS and vanilla JavaScript.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white" alt="HTML5">
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white" alt="CSS3">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black" alt="JavaScript">
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="MIT License">
</p>

## 📦 About the Project

**Dismar Stock** is a browser-based inventory management system designed to make product, supplier, and stock control simple and organized.

The application provides a complete dashboard with inventory statistics, product and supplier management, stock movement history, reports, low-stock alerts, and theme customization.

Data is currently stored locally in the browser using `localStorage`, allowing the application to run without requiring a backend server or database.

## ✨ Features

### 📊 Dashboard

* Total number of registered products
* Total units currently in stock
* Total inventory value
* Low-stock product count
* Most valuable products
* Recent stock movements
* Quick product search

### 📦 Product Management

* Create products
* Edit existing products
* Delete products
* SKU identification
* Product categories
* Unit price management
* Quantity control
* Minimum stock level configuration
* Supplier association
* Product search
* Low-stock filtering
* Out-of-stock filtering

### 🏢 Supplier Management

* Create suppliers
* Edit supplier information
* Delete suppliers
* Store CNPJ
* Phone number
* Email address
* Physical address
* View the number of products associated with each supplier
* Prevent deletion of suppliers that still have linked products

### 🔄 Stock Movements

* Register stock entries
* Register stock exits
* Prevent negative stock quantities
* Automatically record stock changes
* Store movement date and time
* Track previous and new quantities
* Filter movements by entry or exit

### 📈 Reports

* Average inventory value per product
* Number of registered categories
* Number of stock movements
* Inventory value grouped by category
* Percentage distribution of inventory value

### ⚠️ Inventory Alerts

* Low-stock warnings
* Out-of-stock warnings
* Configurable minimum stock levels
* Quick stock replenishment actions

### 🌙 Interface

* Light theme
* Dark theme
* Persistent theme preference
* Dashboard-style interface
* Clean and organized navigation

---

## 🛠️ Tech Stack

| Technology   | Purpose                                |
| ------------ | -------------------------------------- |
| HTML5        | Application structure                  |
| CSS3         | Layout and visual styling              |
| JavaScript   | Application logic and DOM manipulation |
| LocalStorage | Browser-side data persistence          |
| Google Fonts | Inter font family                      |

> **Note:** The Supabase JavaScript library is currently included in the project, but application data is still persisted using `localStorage`.

---

## 📁 Project Structure

```text
dismar_stock/
├── index.html            # Main application page
├── style.css             # Application styles
├── script.js             # Application logic and data management
├── dismar_PNG.png        # Application icon
├── logo_dismar_PNG.png   # Project logo
├── LICENSE               # MIT License
└── README.md             # Project documentation
```

---

## 🚀 Getting Started

Dismar Stock is a front-end application and does not currently require dependency installation.

### 1. Clone the repository

```bash
git clone https://github.com/sknpz/dismar_stock.git
```

### 2. Open the project directory

```bash
cd dismar_stock
```

### 3. Run the application

Open:

```text
index.html
```

directly in your browser.

For development, you can also run the project using a local web server such as the **Live Server** extension for Visual Studio Code.

---

## 💾 Data Storage

Dismar Stock currently stores application data directly in the user's browser using `localStorage`.

The following information is stored:

* Products
* Suppliers
* Stock movements
* Theme preferences
* Application configuration

Because the data is stored locally, it is associated with the browser and device being used.

Clearing the browser's local storage will remove the saved inventory data.

---

## ⚠️ Current Limitations

The current version does not yet include:

* User authentication
* Cloud database synchronization
* Multi-user accounts
* Multi-company support
* Automatic backups
* Cross-device synchronization
* Server-side data persistence

---

## 🗺️ Roadmap

Future improvements may include:

* [ ] Integrate Supabase as the main database
* [ ] Add user authentication
* [ ] Add multi-user support
* [ ] Add company/workspace accounts
* [ ] Add CSV export
* [ ] Add PDF report generation
* [ ] Add interactive dashboard charts
* [ ] Add automated tests
* [ ] Improve mobile responsiveness
* [ ] Add backup and restore functionality
* [ ] Add cloud synchronization
* [ ] Add advanced inventory analytics

---

## 🎯 Project Goals

Dismar Stock was created as a practical inventory management solution while also serving as a project for developing and applying skills involving:

* Front-end development
* JavaScript
* CRUD operations
* Data persistence
* UI/UX design
* Inventory management logic
* Business rules
* Software organization

---

## 📄 License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for more information.

---

## 👨‍💻 Author

Developed by **Matheus Sakuno**.

GitHub: [@sknpz](https://github.com/sknpz)

---

<p align="center">
  Made with ☕ and code.
</p>
