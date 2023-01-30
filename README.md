# LiveSplitAddRun

This program allows you to add a run to your .lss file by inserting a list of times. The program is very fragile and is not meant to work for all use cases *yet*.

### How to use

- Paste your current .lss file into SPLITS.lss.
  - The .lss file must have at least one run done already. If you do not, it is easy to paste all your times directly into the split editor within LiveSplit
- Past your times into TIMES.txt
  - Ensure the times include hours, minutes, and seconds: 00:03:20 means 3 minutes and 20 seconds.

Alternatively, you can use your own files as command line arguments where your .lss file is arg0 and times is arg1.

The new file will be created in the same directory as your splits file.
