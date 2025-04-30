#!/bin/bash

echo "Stopping all containers..."
docker stop $(docker ps -aq)

echo "Removing all containers..."
docker rm $(docker ps -aq)

echo "Removing all unused volumes..."
docker volume prune -f

echo "Removing all unused networks..."
docker network prune -f

echo "Removing all unused images..."
docker image prune -f

echo "Cleanup complete!"
