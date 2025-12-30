## Quick start

If you don't have Scala CLI installed yet, please follow these [installation instructions](https://scala-cli.virtuslab.org/install).

This is a single-file Scala CLI project. You can run it directly with:

```shell
scala-cli --power run *.scala
```

To compile the project:

```shell
scala-cli --power compile *.scala
```

To format the code:

```shell
scala-cli --power fmt *.scala
```

Alternatively, you can use Scala CLI via a docker image:

```shell
docker run -ti --rm -p '8080:8080' -v $(pwd):/app virtuslab/scala-cli run /app/*.scala
```

For more details check the [Scala CLI commands](https://scala-cli.virtuslab.org/docs/commands/basics) page.

## Links:

* [tapir documentation](https://tapir.softwaremill.com/en/latest/)
* [tapir github](https://github.com/softwaremill/tapir)
* [bootzooka: template microservice using tapir](https://softwaremill.github.io/bootzooka/)
* [Scala CLI](https://scala-cli.virtuslab.org)

