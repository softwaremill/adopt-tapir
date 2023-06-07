## Quick start

If you don't have Scala CLI installed yet, please follow these [installation instructions](https://scala-cli.virtuslab.org/install).
You can use the following commands to compile, test and run the project:

```shell
scala-cli --power compile --test . # build the project ('--test' means that tests will be also compiled)
scala-cli --power test . # run the tests
scala-cli --power run . # run the application (Main)
scala-cli --power fmt --check . # run scalaformat check on all scala files and print summary, removing '--check' fixes badly formatted files
```

If you use Scala CLI experimental version, you can use command `scala` instead of `scala-cli`.
More information about Scala CLI as `scala` you can find on this [Scala CLI command: scala](https://scala-cli.virtuslab.org/docs/reference/scala-command/#testing-scala-cli-as-scala) page.

```shell
scala --power compile --test . # build the project ('--test' means that tests will be also compiled)
scala --power test . # run the tests
scala --power run . # run the application (Main)
scala --power fmt --check . # run scalaformat check on all scala files and print summary, removing '--check' fixes badly formatted files
```

To open project in the IDE (Metals / IntelliJ) run any of the `compile` or `test` command above and open the project.
IDE should detect a BSP project and import it.

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
