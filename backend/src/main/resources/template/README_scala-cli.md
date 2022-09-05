## Quick start

If you don't have Scala CLI installed yet, please follow these [installation instructions](https://scala-cli.virtuslab.org/install).
You can use the following commands to compile, test and run the project:

```shell
scala-cli compile --test . # build the project ('--test' means that tests will be also compiled)
scala-cli test . # run the tests
scala-cli run . # run the application (Main)
scala-cli fmt --check . # run scalaformat check on all scala files and print summary, removing '--check' fixes badly formatted files
```

Alternatively, you can use Scala CLI via a docker image:

```shell
docker run -ti --rm -v $(pwd):/app virtuslab/scala-cli compile --test /app # build the project ('--test' means that tests will be also compiled)
docker run -ti --rm -v $(pwd):/app virtuslab/scala-cli test /app # run the tests
docker run -ti --rm -p '8080:8080' -v $(pwd):/app virtuslab/scala-cli run /app # run the application (Main)
```

For more details check the [Scala CLI commands](https://scala-cli.virtuslab.org/docs/commands/basics) page.

## Links:

* [tapir documentation](https://tapir.softwaremill.com/en/latest/)
* [tapir github](https://github.com/softwaremill/tapir)
* [bootzooka: template microservice using tapir](https://softwaremill.github.io/bootzooka/)
* [Scala CLI](ttps://scala-cli.virtuslab.org)
