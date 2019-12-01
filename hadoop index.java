import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.PropertyConfigurator;

public class A4_q2 {
	
	public static class A4_q2Mapper extends Mapper<Object,Text,Text,Text>{
		private Text word = new Text();//a variable to store key
		private Text value1 = new Text();//a variable to store value
		private FileSplit split;//set to split the file
		private String line1;//the string types of line number
		private int linenum = 0;//set a value to record the line number
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			split = (FileSplit) context.getInputSplit();
			String line = value.toString().replaceAll("[^a-zA-Z]", " ");//the regular expression to drop the numbers and punctuation
			StringTokenizer itr = new StringTokenizer(line);
			linenum++;//update the line number after read a line
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken() + "-" + split.getPath().getName().toString() + ":");//set the word value and the filename as the key, and combine them with '-
				line1 = Integer.toString(linenum);//change to String Since the output type is Text
				value1.set(line1);//set the value
				context.write(word, value1);//output
			}
		}
	}
	
	public static class A4_q2Combiner extends Reducer<Text,Text,Text,Text>{
		private Text word = new Text();//key
		private Text filename = new Text();//value
		public void reduce(Text key, Iterable<Text> values, Context context)
		    throws IOException, InterruptedException{
			String[] st = key.toString().split("-");//since we combine the word and filename, and now we split it
			String wd = st[0];
			String fn = st[1];
			
			for (Text val:values) {
				fn+=" "+val.toString()+" ";//combine the line number and the filename
			}
			
			//set the key and the value
            filename.set(fn);
            word.set(wd.toString());
			context.write(word, filename);//output
		}
		
	}
	
	public static class A4_q2Reducer extends Reducer<Text, Text, Text, Text>{
		Text word = new Text();
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
		    InterruptedException{
			String wd = new String();
			for (Text val:values) {
				wd = wd+"["+val.toString()+"]";//combine the word and the (filename+line number)
			}
			word.set(wd);
			context.write(key,  word);
		}
	}
	
	
	
	public static void main(String[] args) throws Exception{
		PropertyConfigurator.configure("config/log4j.properties");
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf,"word count");
		job.setJarByClass(A4_q2.class);
		
		job.setMapperClass(A4_q2Mapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		
		job.setCombinerClass(A4_q2Combiner.class);

		job.setReducerClass(A4_q2Reducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));//input file path
		FileOutputFormat.setOutputPath(job, new Path(args[1]));//output file path

//    	FileInputFormat.addInputPath(job, new Path("hdfs://master-1730026167:9000/data/ws/ws4"));//input file path
//		FileOutputFormat.setOutputPath(job, new Path("hdfs://master-1730026167:9000/output/a4_q2_output"));//output file path
		System.exit(job.waitForCompletion(true) ?0:1);
	}
}