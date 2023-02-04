package LiveSplitAddRun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.time.LocalDateTime;

public class Main {

    public static String convert(String fileName, long[] times) throws IOException {
        ArrayList<String> contents = new ArrayList<>(times.length * 5);
        Scanner s = new Scanner(new File(fileName));
        while (s.hasNext()) {
            contents.add(s.nextLine());
        }

        boolean attemptLooker = false;
        boolean attemptAdded = false;
        boolean pbNext = false;
        boolean bstNext = false;
        boolean inSegmentHistory = false;
        boolean firstRun = false;
        long oldPB = getCurrentPB(contents);
        if (oldPB == -1) oldPB = times[times.length-1] + 1;
        boolean runIsPB = times[times.length - 1] < oldPB;
        int id = -1;
        int split = 1;

        for (int i = 0; i < contents.size(); i++) {
            String line = contents.get(i);

            // Update attempt count
            if (line.startsWith("<AttemptCount>", 2)) {
                id = Integer.parseInt(line.substring(16, line.indexOf("<", 16))) + 1;
                contents.set(i, "  <AttemptCount>" + id + "</AttemptCount>");
                continue;
            }
            // Find where to add attempt info
            if (attemptStarter(line) && !attemptAdded) {
                attemptLooker = true;
                continue;
            }

            if (line.equals("  <AttemptHistory />")) {
                contents.set(i, "  </AttemptHistory>");
                attemptLooker = true;
                firstRun = true;
            }
            // Add attempt info
            if (attemptLooker) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = now.minusSeconds(times[times.length-1]);
                contents.add(i, "    <Attempt id=\"" + id + "\" started=\"" + formatTime(start)
                        + "\" isStartSynced=\"True\" ended=\"" + formatTime(now) + "\" isEndedSynced=\"True\">");
                contents.add(i+1, "      <RealTime>" + formatSeconds(times[times.length-1]) + "</RealTime>");
                contents.add(i+2, "    </Attempt>");
                if (firstRun) contents.add(i, "  <AttemptHistory>");
                attemptAdded = true;
                attemptLooker = false;
                continue;
            }

            // Update PBs
            if (runIsPB) {
                if (line.startsWith("<SplitTime ", 8)) {
                    pbNext = true;
                    continue;
                }
                if (pbNext) {
                    int n;
                    if (line.startsWith("</", n = 8) || line.startsWith("</", n = 6)) {
                        contents.set(i-1, "        <SplitTime name=\"Personal Best\">");
                        contents.add(i, "          <RealTime>" + formatSeconds(times[split]) + "</RealTime>");
                        if (n == 6) contents.add(i+1, "        </SplitTime>");
                    }
                    else {
                        contents.set(i, "          <RealTime>" + formatSeconds(times[split]) + "</RealTime>");
                    }
                    pbNext = false;
                    continue;
                }
            }

            // Update BSTs
            if (line.startsWith("<BestSegmentTime", 6)) {
                bstNext = true;
                continue;
            }
            if (bstNext) {
                if (line.startsWith("</", 6)) {
                    contents.remove(i);
                    line = contents.get(i);
                }
                if (line.startsWith("<S", 6)) {
                    contents.set(i-1, "      <BestSegmentTime>");
                    contents.add(i, "        <RealTime>" + formatSeconds(times[split] - times[split-1]) + "</RealTime>");
                    contents.add(i+1, "      </BestSegmentTime>");
                }
                else {
                    if (times[split] < hmsToSeconds(line.substring(18))) {
                        contents.set(i, "        <RealTime>" + formatSeconds(times[split] - times[split-1]) + "</RealTime>");
                    }
                }
                bstNext = false;
                continue;
            }
            // Update Segment History
            if (line.startsWith("<SegmentHistory />", 6) && !inSegmentHistory) {
                contents.set(i, "      </SegmentHistory>");
                contents.add(i, "        </Time>");
                contents.add(i, "          <RealTime>" + formatSeconds(times[split] - times[split-1]) + "</RealTime>");
                contents.add(i, "        <Time id=\"" + id + "\">");
                contents.add(i, "      <SegmentHistory>");
                split++;
                continue;
            }

            if (line.startsWith("<SegmentHistory", 6)) {
                inSegmentHistory = true;
                continue;
            }
            if (inSegmentHistory) {
                if (line.startsWith("</SegmentHistory", 6)) {
                    contents.add(i, "        </Time>");
                    contents.add(i, "          <RealTime>" + formatSeconds(times[split] - times[split-1]) + "</RealTime>");
                    contents.add(i, "        <Time id=\"" + id + "\">");
                    split++;
                    inSegmentHistory = false;
                }
            }

        }

        String name = fileName + "_updated.lss";
        File ret = new File(name);
        int n = 0;
        while (!ret.createNewFile()) {
            name = fileName + "_updated(" + n + ").lss";
            ret = new File(name);
            n++;
        }

        FileWriter writer = new FileWriter(ret);
        for (String line : contents) {
            writer.write(line+"\n");
        }
        writer.close();

        return "Created file at: " + name;
    }

    private static boolean attemptStarter(String line) {
        return  line.startsWith("<Attempt", 4) ||
                line.startsWith("</Attempt", 4) ||
                line.startsWith("<RealTime", 6) ||
                line.startsWith("<PauseTime", 6);
    }

    private static String formatTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss"));
    }

    private static String formatSeconds(long seconds) {
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        return ((days > 0) ? days + "." : "") +
                (Long.toString(hours).length() != 1 ? hours : "0" + hours) +
                ":" + (Long.toString(minutes).length() != 1 ? minutes : "0" + minutes) +
                ":" + (Long.toString(seconds).length() != 1 ? seconds : "0" + seconds) +
                ".0000000";
    }

    private static long hmsToSeconds(String hms) {

        int indices = 0;
        boolean daysLong = hms.charAt(1) == '.';
        if (daysLong) indices += 2;
        int days = daysLong ? Integer.parseInt(hms.substring(0, 1)) : 0;
        int hours = Integer.parseInt(hms.substring(indices, 2 + indices));
        int mins = Integer.parseInt(hms.substring(3 + indices, 5 + indices));
        int secs = Integer.parseInt(hms.substring(6 + indices, 8 + indices));

        return secs + 60L * mins + 3600L * hours + 86400L * days;
    }

    private static long getCurrentPB(ArrayList<String> lines) {
        for (int i = lines.size() - 1; i > 0; i--) {
            String line = lines.get(i);
            if (line.startsWith("<SplitTime name=\"Per", 8)) {
                return (lines.get(i+1).startsWith("</SplitTime", 8)) ? -1 : hmsToSeconds(lines.get(i + 1).substring(20, 36));
            }
        }
        throw new IllegalStateException("WTF???");
    }

    public static long[] getTimes(File f) throws FileNotFoundException {
        Queue<Long> times = new LinkedList<>();
        Scanner s = new Scanner(f);

        while (s.hasNext()) {
            times.add(hmsToSeconds(s.nextLine()));
        }

        long[] ret = new long[times.size() + 1];
        ret[0] = 0;
        int size = times.size() + 1;
        for (int i = 1; i < size; i++) {
            ret[i] = times.remove();
        }
        return ret;
    }
    public static long[] getTimes(String path) throws FileNotFoundException {
        return getTimes(new File(path));
    }

    public static void main(String[] args) {
        new UI();
    }
}