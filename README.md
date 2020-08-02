# UCSDUnfoldingMaps
In the final piece of your project, you will organize the earthquake data and compute statistics on it. You will then extend your project and share your work with other learners in this class.

To complete this project, follow these steps:

1. Find and open the starter code: You will add functionality to the classes you developed in the previous part of the project: EarthquakeCityMap.java , EarthquakeMarker.java , CityMarker.java , CommonMarker.java, LandQuakeMarker.java , OceanQuakeMarker.java. You will find these files (with our implementations) in the module6 package. You will also find three new files: AirportMap.java, AirportMarker.java and LifeExpectancy.java. These are files you might use in the extension (step 5, below). IF YOU ARE WORKING OFFLINE, you should once again make sure the variable offline is set to true.

2. Compare our implementation to yours. Look at the code and notice any differences between the way you implemented the functionality in module 5 and the way we did. Find at least one difference between your code and ours (could be helper method organization, variable names, the way we implemented a method, etc) and be ready to reflect on it for the quiz.

3. Implement the Comparable interface in EarthquakeMarker:

add the keywords “implements Comparable<EarthquakeMarker>” to the class definition. After you change the header, but before you do step b, make a note of what error(s) occur in your code, where they occurred and why. We’ll ask you about this on the quiz.
implement the compareTo(EarthquakeMarker marker) method in the EarthquakeMarker class so that it sorts earthquakes in reverse order of magnitude.
4. Add and Implement the private method void sortAndPrint(int numToPrint) in EarthquakeCityMap. This method will create a new array from the list of earthquake markers (hint: there is a method in the List interface named toArray() which returns the elements in the List as an array of Objects). Then it will sort the array of earthquake markers in reverse order of their magnitude (highest to lowest) and then print out the top numToPrint earthquakes. If numToPrint is larger than the number of markers in quakeMarkers, it should print out all of the earthquakes and stop, but it should not crash.Call this method from setUp() to test it. An example input and output files are provided in the data folder: use test2.atom as the input file, and sortandPrint.test2.out.txt is the expected output for a couple different calls to sortAndPrint.

5. Add your own extension! This is the fun part and the main part of this final programming assignment. Add any extension you choose to the earthquake program as it stands. You will submit this extension for peer grading, as described in the Peer Review Assignment at the end of this module, following the Programming Assignment Quiz.

The only conditions for this piece is that your extension be (1) something you are interested in doing and (2) something that was not required in any of the previous programming assignments in this course. Also, you will be asked to submit a Peer Review assignment talking about your extension, so it would be a good idea to read over the requirements for that assignment before you get started programming. (It's the last assignment in this module).

We encourage you to get creative, but if you need a little inspiration, here are some ideas you are welcome to use along two different lines: direct extensions to the earthquake application, and working with other data sets. Again, these are just ideas. You are not required to use any of them.

