
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
            SwingUtilities.invokeLater(() -> new GraphWindow());
        } else {
            executarTerminal();
        }
    }

    // =========================
    // EXECUCAO PELO TERMINAL
    // =========================

    private static void executarTerminal() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================");
        System.out.println("          COLORAPRO");
        System.out.println("     COLORACAO DE GRAFOS");
        System.out.println("==================================");

        try {
            System.out.print("Quantidade de disciplinas: ");
            int n = scanner.nextInt();

            System.out.print("Quantidade de conflitos: ");
            int m = scanner.nextInt();

            Graph graph = new Graph(n);

            System.out.println("\nInforme os conflitos (vertice1 vertice2):");

            for (int i = 0; i < m; i++) {
                int u = scanner.nextInt();
                int v = scanner.nextInt();

                graph.addEdge(u, v);
            }

            ColoringResult result =
                    GreedyColoring.color(graph);

            System.out.println("\n===== RESULTADO =====");

            System.out.println("Ordem de processamento:");

            for (int vertex : result.order) {
                System.out.print(vertex + " ");
            }

            System.out.println("\n\nHorarios atribuidos:");

            for (int i = 1; i <= graph.getVertexCount(); i++) {
                System.out.println(
                    "Disciplina " + i +
                    " -> Horario " + result.colors[i]
                );
            }

            System.out.println(
                "\nQuantidade de horarios utilizados: "
                + result.colorCount
            );

            System.out.println("\nProcessamento concluido!");

        } catch (InputMismatchException e) {
            System.out.println("Erro: informe apenas numeros inteiros.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}


// =========================
// CLASSE GRAPH
// =========================

class Graph {

    private final int vertexCount;

    private final List<Set<Integer>> adjacencyList;

    public Graph(int vertexCount) {

        if (vertexCount < 1) {
            throw new IllegalArgumentException(
                "O grafo precisa ter pelo menos um vertice."
            );
        }

        this.vertexCount = vertexCount;

        adjacencyList = new ArrayList<>();

        for (int i = 0; i <= vertexCount; i++) {
            adjacencyList.add(new LinkedHashSet<>());
        }
    }

    public void addEdge(int u, int v) {

        if (u < 1 || u > vertexCount ||
            v < 1 || v > vertexCount) {

            throw new IllegalArgumentException(
                "Vertice fora do intervalo permitido."
            );
        }

        if (u == v) {
            throw new IllegalArgumentException(
                "Nao sao permitidos conflitos de um vertice com ele mesmo."
            );
        }

        adjacencyList.get(u).add(v);
        adjacencyList.get(v).add(u);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public Set<Integer> getNeighbors(int vertex) {
        return Collections.unmodifiableSet(
            adjacencyList.get(vertex)
        );
    }

    public int getDegree(int vertex) {
        return adjacencyList.get(vertex).size();
    }

    public List<Set<Integer>> getAdjacencyList() {
        return Collections.unmodifiableList(adjacencyList);
    }
}


// =========================
// RESULTADO DA COLORACAO
// =========================

class ColoringResult {

    final int[] colors;

    final List<Integer> order;

    final int colorCount;

    public ColoringResult(
        int[] colors,
        List<Integer> order,
        int colorCount
    ) {

        this.colors = colors;
        this.order = order;
        this.colorCount = colorCount;
    }
}


// =========================
// ALGORITMO GULOSO
// HEURISTICA WELSH-POWELL
// =========================

class GreedyColoring {

    public static ColoringResult color(Graph graph) {

        int n = graph.getVertexCount();

        int[] colors = new int[n + 1];

        List<Integer> order = new ArrayList<>();

        // Adiciona todos os vertices.
        for (int i = 1; i <= n; i++) {
            order.add(i);
        }

        // Ordena pelo maior grau.
        // Em caso de empate, usa o menor identificador.
        order.sort(
            Comparator.<Integer>
                comparingInt(graph::getDegree)
                .reversed()
                .thenComparingInt(v -> v)
        );

        int colorCount = 0;

        // Percorre os vertices na ordem definida.
        for (int vertex : order) {

            boolean[] used = new boolean[n + 1];

            // Identifica as cores dos vizinhos.
            for (int neighbor : graph.getNeighbors(vertex)) {

                int neighborColor = colors[neighbor];

                if (neighborColor != 0) {
                    used[neighborColor] = true;
                }
            }

            // Procura a primeira cor disponivel.
            int color = 1;

            while (used[color]) {
                color++;
            }

            // Atribui a cor ao vertice.
            colors[vertex] = color;

            colorCount = Math.max(colorCount, color);
        }

        return new ColoringResult(
            colors,
            order,
            colorCount
        );
    }
}


// =========================
// JANELA GRAFICA
// =========================

class GraphWindow extends JFrame {

    private final JTextArea inputArea;

    private final JTextArea resultArea;

    private final GraphPanel graphPanel;

    public GraphWindow() {

        setTitle("ColoraPro - Coloracao de Grafos");

        setSize(1050, 700);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        inputArea = new JTextArea();

        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 15));

        inputArea.setText(
            "5 5\n" +
            "1 2\n" +
            "1 3\n" +
            "2 3\n" +
            "2 4\n" +
            "3 5\n"
        );

        resultArea = new JTextArea();

        resultArea.setEditable(false);

        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        graphPanel = new GraphPanel();

        JButton executeButton =
            new JButton("Executar algoritmo");

        executeButton.addActionListener(
            e -> processGraph()
        );

        JPanel leftPanel = new JPanel(new BorderLayout());

        leftPanel.add(
            new JLabel(" Entrada do grafo (N M e depois as arestas):"),
            BorderLayout.NORTH
        );

        leftPanel.add(
            new JScrollPane(inputArea),
            BorderLayout.CENTER
        );

        leftPanel.add(
            executeButton,
            BorderLayout.SOUTH
        );

        JPanel rightPanel = new JPanel(new BorderLayout());

        rightPanel.add(
            new JLabel(" Visualizacao do grafo"),
            BorderLayout.NORTH
        );

        rightPanel.add(
            graphPanel,
            BorderLayout.CENTER
        );

        JPanel bottomPanel = new JPanel(new BorderLayout());

        bottomPanel.add(
            new JLabel(" Resultado da coloracao"),
            BorderLayout.NORTH
        );

        bottomPanel.add(
            new JScrollPane(resultArea),
            BorderLayout.CENTER
        );

        JSplitPane topSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            rightPanel
        );

        topSplit.setResizeWeight(0.35);

        JSplitPane mainSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            topSplit,
            bottomPanel
        );

        mainSplit.setResizeWeight(0.70);

        add(mainSplit);

        setVisible(true);

        processGraph();
    }

    private void processGraph() {

        try {

            Graph graph = parseGraph(inputArea.getText());

            ColoringResult result =
                GreedyColoring.color(graph);

            graphPanel.setData(graph, result);

            StringBuilder output = new StringBuilder();

            output.append("ORDEM DE PROCESSAMENTO:\n");

            for (int vertex : result.order) {
                output.append("Vertice ")
                    .append(vertex)
                    .append(" | Grau: ")
                    .append(graph.getDegree(vertex))
                    .append("\n");
            }

            output.append("\nHORARIOS ATRIBUIDOS:\n");

            for (int i = 1; i <= graph.getVertexCount(); i++) {

                output.append("Disciplina ")
                    .append(i)
                    .append(" -> Horario ")
                    .append(result.colors[i])
                    .append("\n");
            }

            output.append("\nTOTAL DE HORARIOS: ")
                .append(result.colorCount);

            resultArea.setText(output.toString());

            graphPanel.repaint();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Erro nos dados",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Graph parseGraph(String data) {

        Scanner scanner = new Scanner(data);

        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException(
                "Informe a quantidade de vertices."
            );
        }

        int n = scanner.nextInt();

        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException(
                "Informe a quantidade de arestas."
            );
        }

        int m = scanner.nextInt();

        if (m < 0) {
            throw new IllegalArgumentException(
                "A quantidade de arestas nao pode ser negativa."
            );
        }

        Graph graph = new Graph(n);

        for (int i = 0; i < m; i++) {

            if (!scanner.hasNextInt()) {
                throw new IllegalArgumentException(
                    "Faltam vertices nas arestas."
                );
            }

            int u = scanner.nextInt();

            if (!scanner.hasNextInt()) {
                throw new IllegalArgumentException(
                    "Faltam vertices nas arestas."
                );
            }

            int v = scanner.nextInt();

            graph.addEdge(u, v);
        }

        if (scanner.hasNext()) {
            throw new IllegalArgumentException(
                "Existem dados extras na entrada."
            );
        }

        scanner.close();

        return graph;
    }
}


// =========================
// VISUALIZACAO DO GRAFO
// =========================

class GraphPanel extends JPanel {

    private Graph graph;

    private ColoringResult result;

    public GraphPanel() {

        setBackground(Color.WHITE);
    }

    public void setData(
        Graph graph,
        ColoringResult result
    ) {

        this.graph = graph;

        this.result = result;
    }

    private Color getColor(int colorNumber) {

        float hue = (
            (colorNumber - 1) * 0.61803398875f
        ) % 1.0f;

        return Color.getHSBColor(
            hue,
            0.65f,
            0.95f
        );
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        if (graph == null || result == null) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int n = graph.getVertexCount();

        int centerX = getWidth() / 2;

        int centerY = getHeight() / 2;

        int radius = Math.max(
            50,
            Math.min(getWidth(), getHeight()) / 2 - 65
        );

        int nodeSize = Math.max(
            24,
            Math.min(48, 500 / Math.max(n, 1))
        );

        int[] x = new int[n + 1];

        int[] y = new int[n + 1];

        // Posiciona os vertices em circulo.
        for (int i = 1; i <= n; i++) {

            double angle =
                2 * Math.PI * (i - 1) / n;

            x[i] = centerX +
                (int) (radius * Math.cos(angle));

            y[i] = centerY +
                (int) (radius * Math.sin(angle));
        }

        // Desenha as arestas.
        g.setColor(Color.GRAY);

        g.setStroke(new BasicStroke(2));

        for (int u = 1; u <= n; u++) {

            for (int v : graph.getNeighbors(u)) {

                if (u < v) {

                    g.drawLine(
                        x[u], y[u],
                        x[v], y[v]
                    );
                }
            }
        }

        // Desenha os vertices.
        for (int i = 1; i <= n; i++) {

            int colorNumber = result.colors[i];

            g.setColor(getColor(colorNumber));

            g.fillOval(
                x[i] - nodeSize / 2,
                y[i] - nodeSize / 2,
                nodeSize,
                nodeSize
            );

            g.setColor(Color.BLACK);

            g.setStroke(new BasicStroke(2));

            g.drawOval(
                x[i] - nodeSize / 2,
                y[i] - nodeSize / 2,
                nodeSize,
                nodeSize
            );

            g.setFont(new Font("Arial", Font.BOLD, 14));

            String label = String.valueOf(i);

            FontMetrics fm = g.getFontMetrics();

            g.drawString(
                label,
                x[i] - fm.stringWidth(label) / 2,
                y[i] + fm.getAscent() / 3
            );
        }

        g.dispose();
    }
}
