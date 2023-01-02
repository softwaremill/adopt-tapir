## Quick start

### Backend: API

To start the backend:

```sh
./backend-start.sh
```

#### Call tests
The project contains two types of tests unit and resource-heavy tests. The latter is limited to max 2 parallel executions
so that they are not exhausting available resources and as a result fail with a timeout. As a result one needs to specify
that additional group needs to be called:

```shell
sbt ';test ;ItTest / test'
```

Note that `ItTest` can be restricted (or run in parallel) by Scala or JSON implementations e.g.

```shell
SCALA=Scala2 JSON="Circe,No" sbt 'ItTest / test'
```

results in running integration tests only for `Scala 2` configurations that JSON is either disabled (`No`) or using
`Circe` implementation.

### Frontend: webapp

In order to locally run and build frontend webapp you need to have the following tools:

#### Node.js environment

- [Node.js download](https://nodejs.org/en/download/) - any version above 16.0.0 will do just fine, usage of LTS are recommended

#### Yarn package manager

- make sure to install version 1.x (currently 1.22) as Yarn 2 enhances different philosophy and is not backwards compatible,
- it's recommeneded to install yarn with native node package manager npm:

```
npm install --global yarn
```

- or via the shell script:

```
curl -o- -L https://yarnpkg.com/install.sh | bash
```

To start the frontend:

```sh
./frontend-start.sh
```

Please notice, that by default fronted points to the production backend.
This could be changed by specifying the `REACT_APP_SERVER_ADDRESS` environmental variable.
To start the frontend pointing to `http://localhost:9090` run:

```sh
export REACT_APP_SERVER_ADDRESS="http://localhost:9090"; ./frontend-start.sh
```

## Copyright

Copyright (C) 2013-2022 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
