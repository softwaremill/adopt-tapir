## Quick start

If you don't have [scala-cli](https://scala-cli.virtuslab.org/install) installed already, you can use the available docker image:

```shell
docker run -ti --rm -v $(pwd):/app virtuslab/scala-cli compile --test /app # build the project ('--test' means that tests will be also compiled)
docker run -ti --rm -v $(pwd):/app virtuslab/scala-cli test /app # run the tests
docker run -ti --rm -p '8080:8080' -v $(pwd):/app virtuslab/scala-cli run /app # run the application (Main)
```

For more details check the [scala-cli commands](https://scala-cli.virtuslab.org/docs/commands/basics) page.

Otherwise, if scala-cli is already installed, you can use the standard commands:

```shell
scala-cli compile --test . # build the project ('--test' means that tests will be also compiled)
scala-cli test . # run the tests
scala-cli run . # run the application (Main)
scala-cli fmt --check . # run scalaformat check on all scala files and print summary, removing '--check' fixes misformatted files
```

## Links:

* [tapir documentation](https://tapir.softwaremill.com/en/latest/)
* [tapir github](https://github.com/softwaremill/tapir)
* [bootzooka: template microservice using tapir](https://softwaremill.github.io/bootzooka/)
* [scala-cli](ttps://scala-cli.virtuslab.org)
