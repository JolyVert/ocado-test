# Payment Optimizer

## Overview

**Payment Optimizer** is a Java application that assigns the most optimal payment methods to a list of customer orders, considering discount promotions and limits on each payment method.

The goal is to:

* Maximize total savings from discount cards and loyalty points.
* Respect spending limits for each method.
* Use fallback strategies when preferred methods are unavailable.

## Features

* Assigns orders to the best available promotional cards.
* Supports full or partial payments with loyalty points (`PUNKTY`).
* Applies fallback logic when promotions can't be applied due to limits.
* Reads input from JSON files (`orders.json`, `paymentmethods.json`).
* Returns aggregated payments per method in a readable format.

## Input Files

### `orders.json`

A list of orders with their total value and applicable promotions:

```json
[
  {
    "id": "ORDER1",
    "value": "100.00",
    "promotions": ["mZysk"]
  },
  {
    "id": "ORDER2",
    "value": "200.00",
    "promotions": ["BosBankrut"]
  }
]
```

### `paymentmethods.json`

A list of payment methods with their discount percentage and usage limit:

```json
[
  {
    "id": "PUNKTY",
    "discount": "15",
    "limit": "100.00"
  },
  {
    "id": "mZysk",
    "discount": "10",
    "limit": "180.00"
  }
]
```

## Example Output

```
mZysk 165.00
BosBankrut 190.00
PUNKTY 100.00
```

## Usage

### Requirements

* Java 17+
* Maven

### Run

```bash
mvn clean package
java -jar target/payment-optimizer.jar path/to/orders.json path/to/paymentmethods.json
```

### Run Tests

```bash
mvn test
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       └── service/
│   │           ├── PaymentOptimizer.java
│   │           ├── JsonLoader.java
│   │       └── model/
│   │           ├── Order.java
│   │           └── PaymentMethod.java
│   │       ├──Main.java
├── test/
│   └── java/
│       └── com/example/PaymentOptimizerTest.java
```

