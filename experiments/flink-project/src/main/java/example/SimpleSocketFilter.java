package example;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SimpleSocketFilter {

  public static void main(String[] args) throws Exception {
    // set up the stream execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Checking input parameters
    final ParameterTool params = ParameterTool.fromArgs(args);

    // make parameters available in the web interface
    env.getConfig().setGlobalJobParameters(params);

    DataStream <String> text = env.socketTextStream("localhost", 9999);

    DataStream <Tuple2 <String, Integer>> counts = text.filter(new FilterFunction <String> () {
        public boolean filter(String value) {
          return value.startsWith("G");
        }
      })

      .map(new Tokenizer()) // split up the lines in pairs (2-tuples) containing: tuple2 {(name,1)...}
      .keyBy(t -> t.f0)
      .sum(1); // group by the tuple field "0" and sum up tuple field "1"

    counts.print();

    // execute program
    env.execute("Streaming Socket Filter");
  }

  public static final class Tokenizer implements MapFunction < String, Tuple2 < String, Integer >> {
    public Tuple2 < String,
    Integer > map(String value) {
      return new Tuple2 < String, Integer > (value, Integer.valueOf(1));
    }
  }

}