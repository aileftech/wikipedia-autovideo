# Wikipedia Autovideo

This Java program automatically creates narrated videos starting from Wikipedia pages,  using images from the page itself and other sources (Pixabay), and doing speech synthesis for the text using Amazon Polly API. If you want to read an in-depth description of how this tool works you can [read more here](https://aileftech.wordpress.com/2020/04/29/turn-any-wikipedia-article-into-a-video-automatically/).

# How to run
You need a Java version >= 8 and Maven installed to build the project (you can also building without Maven manually).

1. Run `mvn package` in the root directory of the project. This should build without errors and create a runnable jar file named `autovideo-0.0.1-SNAPSHOT-jar-with-dependencies.jar` in the `target` directory.
2. Before running, you need to rename the `autovideo.conf.example` file into `autovideo.conf` and fill in your API keys for Pixabay and Amazon Polly.
3. At this point you are ready to run, for example:
```
java -jar target/autovideo-0.0.1-SNAPSHOT-jar-with-dependencies.jar New_York neural
```
This will retrieve the New York Wikipedia page and create the video using Amazon neural engine for speech synthesis. 
This is higher quality but more expensive; the other option is to use `standard` instead of neural, especially for debug, as it is cheaper.

## Dependencies
This tool uses ffmpeg, so you need it installed with a version that's compatible with the ffmpeg filters that we are using. Can't tell an exact number but any version released after 2020 should be ok.

## Output
The program outputs is stored in the `output/final` directory. As the program runs, you will see files like `video.final.0.mp4`, `video.final.1.mp4` in the output directory: 
these are just intermediate steps that are later joined in a single video, but you can still watch them to see how the result is coming out. 

Before starting with the actual video generation, the program also creates an HTML file in `output/final` which provides a 
preview of the final video. It shows which images will end up in the video, how the video is divided in sections, with an estimated length for each one, and what text will be used.
