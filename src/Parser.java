import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public final class Parser {
    public static ProblemInstance parse(String path, boolean oneBased) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String headerLine = nextNonEmptyLine(br);
            if (headerLine == null) {
                throw new IllegalArgumentException("Empty input file");
            }
            StringTokenizer st = new StringTokenizer(headerLine);
            int n = nextInt(st, "n");
            int m = nextInt(st, "m");
            int s = nextInt(st, "s");
            int t = nextInt(st, "t");
            Integer F = null;
            if (st.hasMoreTokens()) {
                F = nextInt(st, "F");
            }

            if (oneBased) {
                s -= 1;
                t -= 1;
            }

            ProblemInstance.Builder b = ProblemInstance.builder(n, m, s, t, F);
            for (int i = 0; i < m; i++) {
                String line = nextNonEmptyLine(br);
                if (line == null) {
                    throw new IllegalArgumentException("Expected " + m + " arc lines but file ended early at line " + (i + 2));
                }
                StringTokenizer est = new StringTokenizer(line);
                int u = nextInt(est, "u");
                int v = nextInt(est, "v");
                int cap = nextInt(est, "cap");
                int cost = nextInt(est, "cost");

                if (oneBased) {
                    u -= 1;
                    v -= 1;
                }

                if (u < 0 || u >= n || v < 0 || v >= n) {
                    throw new IllegalArgumentException("Arc endpoint out of range: (" + u + "," + v + ") with n=" + n);
                }
                if (cap < 0) {
                    throw new IllegalArgumentException("Capacity must be >=0, got " + cap);
                }
                b.addArc(u, v, cap, cost);
            }
            return b.build();
        }
    }

    private static String nextNonEmptyLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                return line;
            }
        }
        return null;
    }

    private static int nextInt(StringTokenizer st, String name) {
        if (!st.hasMoreTokens()) {
            throw new IllegalArgumentException("Missing token for " + name);
        }
        return Integer.parseInt(st.nextToken());
    }
}
