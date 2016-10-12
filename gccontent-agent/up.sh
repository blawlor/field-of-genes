docker-compose -f docker-compose.yml -p gc up -d
docker-compose -f docker-compose.yml -p gc scale loader=32
