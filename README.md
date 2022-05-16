## Quick start

### Backend: PostgreSQL & API

You can spin up DB one easily using docker:

```sh
# use "adopttapir" as a password
docker run --name bootzooka-postgres -p 5432:5432 -e POSTGRES_PASSWORD=adopttapir -e POSTGRES_DB=adopttapir -d postgres
```

Then, you can start the backend:

```sh
export SQL_PASSWORD=adopttapir
./backend-start.sh
```

## Copyright

Copyright (C) 2013-2020 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
