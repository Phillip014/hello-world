
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.log4j.PropertyConfigurator;

public class A4_q3 {
	
	public static class A4_q3Mapper1 extends Mapper<LongWritable,Text,Text,Text>{
		String File_name = ""; 
		int all=0; //count the total of the word
		static Text one = new Text("1");
		String word;

		//the function to account the total word number
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			line = line.replaceAll("[^a-zA-Z]", " ");
			line=line.toLowerCase();//A->a
			InputSplit inputSplit = context.getInputSplit();
			String str = ((FileSplit) inputSplit).getPath().toString();//get the file path
			File_name = str.substring(str.lastIndexOf("/")+1); //get the file name
			StringTokenizer itr = new StringTokenizer(line);
			
			while(itr.hasMoreTokens()) {
				//word = File_name + " " + itr.nextToken();
				word = itr.nextToken() + " " + File_name;
				all++;
				context.write(new Text(word), one);//put word+Filename as the key
			}
			
			}
		
		public void cleanup(Context context) throws IOException, InterruptedException {
			//at the end of map, we write the total number in it
			//Since we need the total number of word to calculate the TF-IDF value
			String str = "" + all;//change int value to string
			
			context.write(new Text(File_name + " " + "!"), new Text(str));
			//Since the ASCII value of "!" is smaller than all the word, when we map, "!" will appear on the first.
		}
		
		}
	
	
	public static class A4_q3Combiner1 extends Reducer<Text,Text,Text,Text>{
		float all = 0;
		
		public void reduce(Text key, Iterable<Text> values, Context context)
		    throws IOException, InterruptedException{
			int index = key.toString().indexOf(" ");
			//the "!" will appear on the first one
			if (key.toString().substring(index+1, index+2).equals("!")) {
				for (Text val:values) {
					//calculate total words
					all = Integer.parseInt(val.toString());
				}
				
				return;
			}
			
			float sum = 0;// count the number of one word appeared
			for (Text val:values) {
				sum += Integer.parseInt(val.toString());
			}
			//one word appeared time is finish calculate. So we can calculate the TF value 
			// TF = sum/all
			float tmp = sum /all;
			String value = "";
			value += tmp;//record the TF value and change it to String

			context.write(new Text(key), new Text(value));
		}
		
	}
	
	public static class A4_q3Reducer1 extends Reducer<Text, Text, Text, Text>{

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
		    InterruptedException{

			for (Text val:values) {
				context.write(key, val);
			}
		}
	}
	
	public static class A4_q3Partitoner extends Partitioner<Text, Text>{
		
		public int getPartition(Text key, Text value, int numPartitions) {
			
			String ip1 = key.toString();
			ip1 = ip1.substring(0, ip1.indexOf(" "));
			Text p1 = new Text(ip1);
			return Math.abs((p1.hashCode() * 127) % numPartitions);
		}
	}
	
	public static class A4_q3Mapper2 extends Mapper<LongWritable, Text, Text, Text>{
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
			String val = value.toString().replaceAll("    ", " ");
			int index = val.indexOf(" ");
			String s1 = val.substring(0, index); //get the key word
			String s2 = val.substring(index + 1); //get the value
			
			 s2 += " ";
			 s2 += "1"; //"1" means appear once time
			 context.write(new Text(s1), new Text(s2)); 
		}
	}
	
	public static class A4_q3Reducer2 extends Reducer<Text, Text, Text, Text> {
		int file_count;
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			file_count = context.getNumReduceTasks(); // get the total number of files
			float sum = 0;
			List<String> vals = new ArrayList<String>();
			for (Text str : values) {
				int index = str.toString().lastIndexOf(" ");
				sum += Integer.parseInt(str.toString().substring(index + 1));// calculate the word appear time in all file
				vals.add(str.toString().substring(0, index)); // save the value
			}
			double tmp = Math.log10(file_count * 1.0 / (sum * 1.0));//IDF value : word appear in all file and divides total file number 
			
			for (int j = 0; j < vals.size(); j++) {
				String val = vals.get(j);
				String end = val.substring(val.lastIndexOf(" "));
				float f_end = Float.parseFloat(end);//read the tf value
				val += " ";
				val += f_end * tmp; //calculate the tf-idf value
				context.write(key, new Text(val));
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		PropertyConfigurator.configure("config/log4j.properties");
		Configuration conf1 = new Configuration();
		
		Job job1 = Job.getInstance(conf1, "My_tdif_part1");

		job1.setJarByClass(A4_q3.class);
		
		job1.setMapperClass(A4_q3Mapper1.class);
		job1.setCombinerClass(A4_q3Combiner1.class);
		job1.setReducerClass(A4_q3Reducer1.class);
		job1.setMapOutputKeyClass(Text.class);
        job1.setMapOutputValueClass(Text.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        
        job1.setPartitionerClass(A4_q3Partitoner.class);

        FileInputFormat.addInputPath(job1, new Path("hdfs://master-1730026167:9000/data/ws/ws4"));
        FileOutputFormat.setOutputPath(job1, new Path("hdfs://master-1730026167:9000/output/project1"));
        System.exit(job1.waitForCompletion(true) ? 0 : 1);

        PropertyConfigurator.configure("config/log4j.properties");
        Configuration conf2 = new Configuration();
      
        Job job2 = Job.getInstance(conf2, "My_tdif_part2");
        job2.setJarByClass(A4_q3.class);
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setMapperClass(A4_q3Mapper2.class);
        job2.setReducerClass(A4_q3Reducer2.class);

        FileInputFormat.addInputPath(job2,  new Path("hdfs://master-1730026167:9000/data/ws/ws4"));
        FileOutputFormat.setOutputPath(job2, new Path("hdfs://master-1730026167:9000/output/project1"));

        System.exit(job2.waitForCompletion(true) ? 0 : 1);


	}
}
	

