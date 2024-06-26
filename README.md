# Project Setup Guide

This guide will help you set up your development environment for a Spring Boot backend and a Vue.js frontend on Ubuntu, along with Docker and PostgreSQL for database management.

## Prerequisites

Ensure your system is up-to-date:

```bash
sudo apt update
```

### 1. Install Java Development Kit (JDK)

Spring Boot requires Java. Install the JDK:

```bash
sudo apt install openjdk-17-jdk
```

### 2. Install Maven

Maven is a build tool for Java projects, commonly used with Spring Boot:

```bash
sudo apt install maven
```

### 3. Install Git

Git is essential for version control:

```bash
sudo apt install git
```

### 4. Install Node.js and npm

Vue.js requires Node.js and npm (Node Package Manager):

```bash
sudo apt install nodejs npm
```

### 5. Install Vue CLI

Vue CLI is a command-line tool to scaffold and manage Vue.js projects:

```bash
sudo npm install -g @vue/cli
```

### 6. Integrated Development Environment (IDE)

You can use an IDE of your choice. Popular choices include IntelliJ IDEA for Java/Spring Boot and Visual Studio Code for JavaScript/Vue.js.

Install IntelliJ IDEA (Community Edition) by downloading and installing it from the [official website](https://www.jetbrains.com/idea/download/).

Install Visual Studio Code:

```bash
sudo snap install code --classic
```

### 7. Database 

Depending on your project, you might need a database. In this case, we'll be using PostgreSQL with Docker.

### 8. Install Docker and Docker Compose

```bash
sudo apt install docker.io
sudo apt install docker-compose
```

## Project Setup

1. Clone the project repository:

```bash
git clone https://github.com/your-repo/your-project.git
```

2. Navigate to the project directory:

```bash
cd your-project
```

3. Install frontend dependencies:

```bash
cd frontend
npm install
```

4. Start the project using Docker Compose:

```bash
docker-compose up --build
```

This will start the backend, frontend, and PostgreSQL database containers.

5. To stop the project, press `Ctrl+C` and then run:

```bash
docker-compose down
```


### PgAdmin Setup

1. Log in to PgAdmin with the following credentials on a running docker container:

```
PGADMIN_DEFAULT_EMAIL: a@a.com
PGADMIN_DEFAULT_PASSWORD: a
```

2. Add a new server by right-clicking on "Servers" and selecting "Create > Server..."

3. In the "Create - Server" window, provide the following details:

   - Name: A name of your choice
   - Host name/address: `db` (the name of the PostgreSQL container)
   - Port: `5432`
   - Maintenance db : `mydatabase`
   - Username: `postgres`
   - Password: `secret`

4. Click "Save" to add the server.

5. Expand the server connection, and you should see the `mydatabase` database listed under "Databases".


### Access the Applications

- Frontend: [http://localhost:8082/](http://localhost:8082/)
- PgAdmin (PostgreSQL Administration): [http://localhost:8083](http://localhost:8083)

### Init Data into the DB uncomment in src/main/resources/application.properties initializeData

## Summary of Commands

Here are the essential commands consolidated:

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven git nodejs npm
sudo npm install -g @vue/cli
sudo snap install code --classic
sudo apt install docker.io
sudo apt install docker-compose

chmod +x pgadmin_entrypoint.sh
chmod +x generate_tree.sh

# Frontend setup
cd frontend
npm install

# Start the project
docker-compose up --build

# Stop the project
Ctrl+C
docker-compose down
```

After completing these steps, you should have a development environment ready for executing the project
