Aggregator

I've used rxjava as streaming API. This technology allows to attach as many calculations/aggregations as required during
processing instrument prices stream. There are many ready operators on streams out of the box such as map, filter etc.
Also the framework supports controlling back pressure.

Wrong lines of input file can be reported back to the stream driver as the failedToParse stream for logging or anything
like that.

MultiplierProvider searches for the multiplier of the particular instrument. At the moment it caches DB lookup results for
5 seconds and provides methods for invalidating cached multipliers to allow DB writing component to invalidate cache after
DB has been changed.

How to run:

StreamDriver.main() starts the task with provided example_input.txt.
Tests can be run as folows
mvn test

MassDataGenerator generates large amount of instrument prices for testing application function.

I profiled the app with Your Kit Profiler. It shows quite low memory consumption.