#!/bin/bash
# Apply database migrations
dotnet ef database update

# Start the application
dotnet Memora.dll