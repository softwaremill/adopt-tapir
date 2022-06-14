## Quick start

### Backend: API

To start the backend:

```sh
./backend-start.sh
```

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

## Copyright

Copyright (C) 2013-2022 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
