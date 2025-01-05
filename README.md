# Location-Tracker-Client

**Location-Tracker-Client** is a lightweight Android application that fetches the user's current location and sends it to a server at a specified IP address.

## Features

- **Location Fetching**: Retrieves the user's precise location (latitude and longitude) using GPS or network providers.
- **Custom Server Configuration**: Allows the user to specify the server's IP address.
- **Periodic Updates**: Option to send location updates at regular intervals.

## Requirements

- Android 6.0 (Marshmallow) or higher.
- Internet connection.
- Location permissions granted by the user.

## Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION` to fetch the user's location.
- `INTERNET` to send data to the server.

## Server
- This app is the location source client for the **location-tracker-server** project.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/kailas098/location-tracker-client.git

