package proj;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class hadindex {
	
	public static class IndexMapper extends Mapper<Object,Text,Text,Text>{
		File filelocation = new File("/home/uic/Downloads/index1234");//the location to store index
		private FileSplit split;
		public void map(Object key, Text value, Mapper<Object,Text,Text,Text>.Context context) throws IOException, InterruptedException {
			//build a director object to store index
			Directory directory = FSDirectory.open(filelocation.toPath());
			split = (FileSplit) context.getInputSplit();//spilt file
			String filename = split.getPath().getName().toString();//get the file name
			
			IndexWriter indexWriter;//initialize a index writer object
			Analyzer analyzer = new StandardAnalyzer(); //to split every English word
			IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
			indexWriter = new IndexWriter(directory, iwConfig);

			String content = value.toString();
			
			//create a document domain, and add the file name and word information in it
			Document document = new Document();
			document.add(new TextField("name", filename, Field.Store.YES));//add the filename in it
			document.add(new TextField("content", content, Field.Store.YES));// add the word value in it

			//put the document object to the index
			indexWriter.addDocument(document);
			
			indexWriter.close();
		}
	}
	
	public static void main(String[] args) throws Exception{
		PropertyConfigurator.configure("config/log4j.properties");
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf,"word count");
		
		job.setJarByClass(hadindex.class);
		job.setMapperClass(IndexMapper.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path("hdfs://master-1730026167:9000/data/small"));
		FileOutputFormat.setOutputPath(job, new Path("hdfs://master-1730026167:9000/output/sluceneindex"));

		
		System.exit(job.waitForCompletion(true) ?0:1);

	}
}
