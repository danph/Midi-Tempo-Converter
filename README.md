# MIDI-Tempo-Converter

This is a simple MIDI file tempo converter application.

Essentially it changes the embedded tempo of a MIDI file and time-shifts all
the MIDI events in-line with the tempo change so that the playback of the file 
is un-affected. It should work on both Type 0 and 1 MIDI file formats.

I've used this to overcome difficulties encountered when trying to merge MIDI files
with different tempos into a single project. Typically projects for example in 
Cubase have a single master tempo so importing midi files with different
base tempos requires some modification and time-shifting of the MIDI files in 
order for them to line up correctly in the project.

Hope you find this a useful little tool for your MIDI projects !

## Installation
1. Application requires a Java runtime environment (JRE) on your machine
2. Download MidiTempoConverter.jar

## Usage
'java -jar MidiTempoConverter.jar file tempo [debug]

Parameters:
 file - Name of your file
 tempo - New tempo (120 for example)
 debug - Optional. Type debug at the end if you want to get a dump of all the midi events in the file

## Example Usage
- File to be converted is 'Test.mid' placed in the same directory as the jar application file
- It's existing tempo is 58
- We want to change it to 120 whilst maintaining the original playback speed

```shell
java -jar MidiTempoConverter.jar Test.mid 120

Midi Tempo Converter 1.0
==========================
Old Tempo: 58
New Tempo: 120
Updating MIDI Events
Found Master Tempo (Set = 120)
Saving Changes to the MIDI File
Done!'
```

Enjoy !
