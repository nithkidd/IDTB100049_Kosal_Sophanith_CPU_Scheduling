import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;

public class CPUSchedulingSimulator extends JFrame {
    private JPanel mainPanel;
    private JComboBox<String> algorithmComboBox;
    private JTextField processCountField;
    private JButton createProcessesButton;
    private JPanel processInputPanel;
    private JPanel resultsPanel;
    private JTextField quantumField;
    private JButton calculateButton;
    private JTextArea ganttChartArea;
    private JTextArea waitingTimeArea;
    private JTextArea turnaroundTimeArea;
    private JTextArea averagesArea;
    private ArrayList<ProcessRow> processRows;
    private DecimalFormat df = new DecimalFormat("#.##");

    public CPUSchedulingSimulator() {
        setTitle("CPU Scheduling Simulator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Algorithm:"));
        
        algorithmComboBox = new JComboBox<>(new String[]{
            "First-Come, First-Served (FCFS)",
            "Shortest-Job-First (SJF)",
            "Shortest-Remaining-Time (SRT)",
            "Round Robin (RR)"
        });
        topPanel.add(algorithmComboBox);
        
        JPanel processPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        processPanel.add(new JLabel("Number of Processes:"));
        processCountField = new JTextField(5);
        processPanel.add(processCountField);
        createProcessesButton = new JButton("Create Process Inputs");
        processPanel.add(createProcessesButton);
        
        quantumField = new JTextField(5);
        processPanel.add(new JLabel("Time Quantum (for RR):"));
        processPanel.add(quantumField);
        
        mainPanel.add(topPanel);
        mainPanel.add(processPanel);
        
        processInputPanel = new JPanel();
        processInputPanel.setLayout(new BoxLayout(processInputPanel, BoxLayout.Y_AXIS));
        JScrollPane processScrollPane = new JScrollPane(processInputPanel);
        processScrollPane.setPreferredSize(new Dimension(750, 200));
        mainPanel.add(processScrollPane);
        
        calculateButton = new JButton("Calculate");
        calculateButton.setEnabled(false);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(calculateButton);
        mainPanel.add(buttonPanel);
        
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        
        ganttChartArea = new JTextArea(3, 40);
        ganttChartArea.setEditable(false);
        JScrollPane ganttScrollPane = new JScrollPane(ganttChartArea);
        ganttScrollPane.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));
        
        waitingTimeArea = new JTextArea(3, 40);
        waitingTimeArea.setEditable(false);
        JScrollPane waitingScrollPane = new JScrollPane(waitingTimeArea);
        waitingScrollPane.setBorder(BorderFactory.createTitledBorder("Waiting Times"));
        
        turnaroundTimeArea = new JTextArea(3, 40);
        turnaroundTimeArea.setEditable(false);
        JScrollPane turnaroundScrollPane = new JScrollPane(turnaroundTimeArea);
        turnaroundScrollPane.setBorder(BorderFactory.createTitledBorder("Turnaround Times"));
        
        averagesArea = new JTextArea(2, 40);
        averagesArea.setEditable(false);
        JScrollPane averagesScrollPane = new JScrollPane(averagesArea);
        averagesScrollPane.setBorder(BorderFactory.createTitledBorder("Averages"));
        
        resultsPanel.add(ganttScrollPane);
        resultsPanel.add(waitingScrollPane);
        resultsPanel.add(turnaroundScrollPane);
        resultsPanel.add(averagesScrollPane);
        
        JScrollPane resultsScrollPane = new JScrollPane(resultsPanel);
        resultsScrollPane.setPreferredSize(new Dimension(750, 250));
        mainPanel.add(resultsScrollPane);
        
        add(mainPanel);
        
        setupEventListeners();
        
        processRows = new ArrayList<>();
    }
    
    private void setupEventListeners() {
        algorithmComboBox.addActionListener(e -> {
            boolean isRR = algorithmComboBox.getSelectedIndex() == 3;
            quantumField.setEnabled(isRR);
            if (!isRR) {
                quantumField.setText("");
            }
        });
        
        createProcessesButton.addActionListener(e -> {
            try {
                int processCount = Integer.parseInt(processCountField.getText());
                if (processCount <= 0) {
                    JOptionPane.showMessageDialog(this, "Number of processes must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                processInputPanel.removeAll();
                processRows.clear();
                
                JPanel headerPanel = new JPanel(new GridLayout(1, 3));
                headerPanel.add(new JLabel("Process ID"));
                headerPanel.add(new JLabel("Arrival Time"));
                headerPanel.add(new JLabel("Burst Time"));
                processInputPanel.add(headerPanel);
                
                for (int i = 1; i <= processCount; i++) {
                    ProcessRow row = new ProcessRow("P" + i);
                    processRows.add(row);
                    processInputPanel.add(row.getPanel());
                }
                
                calculateButton.setEnabled(true);
                processInputPanel.revalidate();
                processInputPanel.repaint();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for process count", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        calculateButton.addActionListener(e -> {
            try {
                ArrayList<Process> processes = new ArrayList<>();
                for (ProcessRow row : processRows) {
                    int arrivalTime = Integer.parseInt(row.getArrivalTime());
                    int burstTime = Integer.parseInt(row.getBurstTime());
                    
                    if (arrivalTime < 0 || burstTime <= 0) {
                        JOptionPane.showMessageDialog(this, "Arrival time must be non-negative and burst time must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    processes.add(new Process(row.getProcessId(), arrivalTime, burstTime));
                }
                
                int algorithmIndex = algorithmComboBox.getSelectedIndex();
                SchedulingAlgorithm algorithm = null;
                
                switch (algorithmIndex) {
                    case 0:
                        algorithm = new FCFS();
                        break;
                    case 1:
                        algorithm = new SJF();
                        break;
                    case 2:
                        algorithm = new SRT();
                        break;
                    case 3:
                        try {
                            int quantum = Integer.parseInt(quantumField.getText());
                            if (quantum <= 0) {
                                JOptionPane.showMessageDialog(this, "Time quantum must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            algorithm = new RoundRobin(quantum);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Please enter a valid number for time quantum", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        break;
                }
                
                SchedulingResult result = algorithm.schedule(processes);
                displayResults(result);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for arrival and burst times", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void displayResults(SchedulingResult result) {
        // Gantt Chart
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        for (GanttChartEntry entry : result.getGanttChart()) {
            ganttChart.append(entry.getProcessId())
                     .append(" (")
                     .append(entry.getStartTime())
                     .append("-")
                     .append(entry.getEndTime())
                     .append(") → ");
        }
        if (ganttChart.length() > 12) {
            ganttChart.setLength(ganttChart.length() - 2); // Remove last arrow
        }
        ganttChartArea.setText(ganttChart.toString());
        
        // Waiting Times
        StringBuilder waitingTimes = new StringBuilder("Waiting Times: ");
        for (Map.Entry<String, Integer> entry : result.getWaitingTimes().entrySet()) {
            waitingTimes.append(entry.getKey())
                       .append(" = ")
                       .append(entry.getValue())
                       .append(", ");
        }
        if (waitingTimes.length() > 15) {
            waitingTimes.setLength(waitingTimes.length() - 2); // Remove last comma
        }
        waitingTimeArea.setText(waitingTimes.toString());
        
        // Turnaround Times
        StringBuilder turnaroundTimes = new StringBuilder("Turnaround Times: ");
        for (Map.Entry<String, Integer> entry : result.getTurnaroundTimes().entrySet()) {
            turnaroundTimes.append(entry.getKey())
                          .append(" = ")
                          .append(entry.getValue())
                          .append(", ");
        }
        if (turnaroundTimes.length() > 18) {
            turnaroundTimes.setLength(turnaroundTimes.length() - 2); // Remove last comma
        }
        turnaroundTimeArea.setText(turnaroundTimes.toString());
        
        // Averages
        StringBuilder averages = new StringBuilder();
        averages.append("Average Waiting Time: ")
                .append(df.format(result.getAverageWaitingTime()))
                .append("\n")
                .append("Average Turnaround Time: ")
                .append(df.format(result.getAverageTurnaroundTime()));
        averagesArea.setText(averages.toString());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CPUSchedulingSimulator simulator = new CPUSchedulingSimulator();
            simulator.setVisible(true);
        });
    }
    
    // Inner class for process input rows
    private class ProcessRow {
        private JPanel panel;
        private JLabel processIdLabel;
        private JTextField arrivalTimeField;
        private JTextField burstTimeField;
        
        public ProcessRow(String processId) {
            panel = new JPanel(new GridLayout(1, 3));
            processIdLabel = new JLabel(processId);
            arrivalTimeField = new JTextField(5);
            burstTimeField = new JTextField(5);
            
            panel.add(processIdLabel);
            panel.add(arrivalTimeField);
            panel.add(burstTimeField);
        }
        
        public JPanel getPanel() {
            return panel;
        }
        
        public String getProcessId() {
            return processIdLabel.getText();
        }
        
        public String getArrivalTime() {
            return arrivalTimeField.getText();
        }
        
        public String getBurstTime() {
            return burstTimeField.getText();
        }
    }
}

// Process class
class Process {
    private String id;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    
    public Process(String id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
    
    public String getId() {
        return id;
    }
    
    public int getArrivalTime() {
        return arrivalTime;
    }
    
    public int getBurstTime() {
        return burstTime;
    }
    
    public int getRemainingTime() {
        return remainingTime;
    }
    
    public void decreaseRemainingTime(int time) {
        this.remainingTime -= time;
        if (remainingTime < 0) {
            remainingTime = 0;
        }
    }
    
    public Process clone() {
        Process clone = new Process(id, arrivalTime, burstTime);
        clone.remainingTime = this.remainingTime;
        return clone;
    }
}

// GanttChartEntry class
class GanttChartEntry {
    private String processId;
    private int startTime;
    private int endTime;
    
    public GanttChartEntry(String processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getProcessId() {
        return processId;
    }
    
    public int getStartTime() {
        return startTime;
    }
    
    public int getEndTime() {
        return endTime;
    }
}

// SchedulingResult class
class SchedulingResult {
    private ArrayList<GanttChartEntry> ganttChart;
    private Map<String, Integer> waitingTimes;
    private Map<String, Integer> turnaroundTimes;
    private double averageWaitingTime;
    private double averageTurnaroundTime;
    
    public SchedulingResult(ArrayList<GanttChartEntry> ganttChart, Map<String, Integer> waitingTimes, 
                          Map<String, Integer> turnaroundTimes, double averageWaitingTime, 
                          double averageTurnaroundTime) {
        this.ganttChart = ganttChart;
        this.waitingTimes = waitingTimes;
        this.turnaroundTimes = turnaroundTimes;
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnaroundTime = averageTurnaroundTime;
    }
    
    public ArrayList<GanttChartEntry> getGanttChart() {
        return ganttChart;
    }
    
    public Map<String, Integer> getWaitingTimes() {
        return waitingTimes;
    }
    
    public Map<String, Integer> getTurnaroundTimes() {
        return turnaroundTimes;
    }
    
    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }
    
    public double getAverageTurnaroundTime() {
        return averageTurnaroundTime;
    }
}

// Interface for scheduling algorithms
interface SchedulingAlgorithm {
    SchedulingResult schedule(ArrayList<Process> processes);
}

// FCFS Algorithm
class FCFS implements SchedulingAlgorithm {
    @Override
    public SchedulingResult schedule(ArrayList<Process> processes) {
        // Create copies of processes to avoid modifying the original list
        ArrayList<Process> processList = new ArrayList<>();
        for (Process p : processes) {
            processList.add(p.clone());
        }
        
        // Sort processes by arrival time
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        ArrayList<GanttChartEntry> ganttChart = new ArrayList<>();
        Map<String, Integer> waitingTimes = new HashMap<>();
        Map<String, Integer> turnaroundTimes = new HashMap<>();
        
        int currentTime = 0;
        
        for (Process process : processList) {
            // If currentTime is less than arrival time, update it
            if (currentTime < process.getArrivalTime()) {
                currentTime = process.getArrivalTime();
            }
            
            int startTime = currentTime;
            int endTime = startTime + process.getBurstTime();
            
            // Calculate waiting time (current time - arrival time)
            int waitingTime = startTime - process.getArrivalTime();
            waitingTimes.put(process.getId(), waitingTime);
            
            // Calculate turnaround time (completion time - arrival time)
            int turnaroundTime = endTime - process.getArrivalTime();
            turnaroundTimes.put(process.getId(), turnaroundTime);
            
            // Add to gantt chart
            ganttChart.add(new GanttChartEntry(process.getId(), startTime, endTime));
            
            // Update current time
            currentTime = endTime;
        }
        
        // Calculate averages
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        for (String processId : waitingTimes.keySet()) {
            totalWaitingTime += waitingTimes.get(processId);
            totalTurnaroundTime += turnaroundTimes.get(processId);
        }
        
        double avgWaitingTime = totalWaitingTime / waitingTimes.size();
        double avgTurnaroundTime = totalTurnaroundTime / turnaroundTimes.size();
        
        return new SchedulingResult(ganttChart, waitingTimes, turnaroundTimes, avgWaitingTime, avgTurnaroundTime);
    }
}

// SJF Algorithm (Non-preemptive)
class SJF implements SchedulingAlgorithm {
    @Override
    public SchedulingResult schedule(ArrayList<Process> processes) {
        // Create copies of processes
        ArrayList<Process> processList = new ArrayList<>();
        for (Process p : processes) {
            processList.add(p.clone());
        }
        
        // Sort by arrival time initially
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        ArrayList<GanttChartEntry> ganttChart = new ArrayList<>();
        Map<String, Integer> waitingTimes = new HashMap<>();
        Map<String, Integer> turnaroundTimes = new HashMap<>();
        
        int currentTime = 0;
        ArrayList<Process> arrivedProcesses = new ArrayList<>();
        int remainingProcesses = processList.size();
        
        while (remainingProcesses > 0) {
            // Add newly arrived processes to the queue
            Iterator<Process> iterator = processList.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.getArrivalTime() <= currentTime) {
                    arrivedProcesses.add(process);
                    iterator.remove();
                }
            }
            
            if (arrivedProcesses.isEmpty()) {
                // No process has arrived yet, move time to next arrival
                if (!processList.isEmpty()) {
                    currentTime = processList.get(0).getArrivalTime();
                    continue;
                }
                break;
            }
            
            // Find shortest job
            arrivedProcesses.sort(Comparator.comparingInt(Process::getBurstTime));
            Process shortestJob = arrivedProcesses.remove(0);
            
            int startTime = currentTime;
            int endTime = startTime + shortestJob.getBurstTime();
            
            // Calculate waiting time
            int waitingTime = startTime - shortestJob.getArrivalTime();
            waitingTimes.put(shortestJob.getId(), waitingTime);
            
            // Calculate turnaround time
            int turnaroundTime = endTime - shortestJob.getArrivalTime();
            turnaroundTimes.put(shortestJob.getId(), turnaroundTime);
            
            // Add to gantt chart
            ganttChart.add(new GanttChartEntry(shortestJob.getId(), startTime, endTime));
            
            // Update current time
            currentTime = endTime;
            remainingProcesses--;
        }
        
        // Calculate averages
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        for (String processId : waitingTimes.keySet()) {
            totalWaitingTime += waitingTimes.get(processId);
            totalTurnaroundTime += turnaroundTimes.get(processId);
        }
        
        double avgWaitingTime = totalWaitingTime / waitingTimes.size();
        double avgTurnaroundTime = totalTurnaroundTime / turnaroundTimes.size();
        
        return new SchedulingResult(ganttChart, waitingTimes, turnaroundTimes, avgWaitingTime, avgTurnaroundTime);
    }
}

// SRT Algorithm (Preemptive SJF)
class SRT implements SchedulingAlgorithm {
    @Override
    public SchedulingResult schedule(ArrayList<Process> processes) {
        // Create copies of processes
        ArrayList<Process> processList = new ArrayList<>();
        for (Process p : processes) {
            processList.add(p.clone());
        }
        
        // Sort by arrival time
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        ArrayList<GanttChartEntry> ganttChart = new ArrayList<>();
        Map<String, Integer> waitingTimes = new HashMap<>();
        Map<String, Integer> turnaroundTimes = new HashMap<>();
        Map<String, Integer> completionTimes = new HashMap<>();
        
        int currentTime = 0;
        int remainingProcesses = processList.size();
        
        // Tracks execution times for each process
        Map<String, Integer> executionTimes = new HashMap<>();
        for (Process p : processes) {
            executionTimes.put(p.getId(), 0);
        }
        
        String currentProcessId = null;
        int currentProcessStartTime = 0;
        
        while (remainingProcesses > 0) {
            // Find the process with shortest remaining time
            Process shortestProcess = null;
            int shortestRemainingTime = Integer.MAX_VALUE;
            
            // Check if any new processes arrived
            for (Process process : processList) {
                if (process.getArrivalTime() <= currentTime && process.getRemainingTime() > 0) {
                    if (process.getRemainingTime() < shortestRemainingTime) {
                        shortestProcess = process;
                        shortestRemainingTime = process.getRemainingTime();
                    }
                }
            }
            
            // If no process is available, move time forward
            if (shortestProcess == null) {
                currentTime++;
                continue;
            }
            
            // Process context switch - add to Gantt chart if there's a change
            if (currentProcessId == null || !currentProcessId.equals(shortestProcess.getId())) {
                if (currentProcessId != null) {
                    ganttChart.add(new GanttChartEntry(currentProcessId, currentProcessStartTime, currentTime));
                }
                currentProcessId = shortestProcess.getId();
                currentProcessStartTime = currentTime;
            }
            
            // Execute process for 1 time unit
            shortestProcess.decreaseRemainingTime(1);
            executionTimes.put(shortestProcess.getId(), executionTimes.get(shortestProcess.getId()) + 1);
            currentTime++;
            
            // If process is complete
            if (shortestProcess.getRemainingTime() == 0) {
                completionTimes.put(shortestProcess.getId(), currentTime);
                
                // Calculate turnaround time
                int turnaroundTime = currentTime - shortestProcess.getArrivalTime();
                turnaroundTimes.put(shortestProcess.getId(), turnaroundTime);
                
                // Calculate waiting time (turnaround time - burst time)
                int waitingTime = turnaroundTime - shortestProcess.getBurstTime();
                waitingTimes.put(shortestProcess.getId(), waitingTime);
                
                remainingProcesses--;
                
                // Add final entry to Gantt chart
                ganttChart.add(new GanttChartEntry(currentProcessId, currentProcessStartTime, currentTime));
                currentProcessId = null;
            }
        }
        
        // Calculate averages
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        for (String processId : waitingTimes.keySet()) {
            totalWaitingTime += waitingTimes.get(processId);
            totalTurnaroundTime += turnaroundTimes.get(processId);
        }
        
        double avgWaitingTime = totalWaitingTime / waitingTimes.size();
        double avgTurnaroundTime = totalTurnaroundTime / turnaroundTimes.size();
        
        // Optimize Gantt chart by combining consecutive entries of the same process
        ArrayList<GanttChartEntry> optimizedGantt = new ArrayList<>();
        if (!ganttChart.isEmpty()) {
            GanttChartEntry currentEntry = ganttChart.get(0);
            
            for (int i = 1; i < ganttChart.size(); i++) {
                GanttChartEntry nextEntry = ganttChart.get(i);
                
                if (currentEntry.getProcessId().equals(nextEntry.getProcessId()) && 
                    currentEntry.getEndTime() == nextEntry.getStartTime()) {
                    // Combine entries
                    currentEntry = new GanttChartEntry(
                        currentEntry.getProcessId(),
                        currentEntry.getStartTime(),
                        nextEntry.getEndTime()
                    );
                } else {
                    optimizedGantt.add(currentEntry);
                    currentEntry = nextEntry;
                }
            }
            
            optimizedGantt.add(currentEntry);
        }
        
        return new SchedulingResult(optimizedGantt, waitingTimes, turnaroundTimes, 
                                  avgWaitingTime, avgTurnaroundTime);
    }
}

// Round Robin Algorithm
class RoundRobin implements SchedulingAlgorithm {
    private int quantum;
    
    public RoundRobin(int quantum) {
        this.quantum = quantum;
    }
    
    @Override
    public SchedulingResult schedule(ArrayList<Process> processes) {
        // Create copies of processes
        ArrayList<Process> processList = new ArrayList<>();
        for (Process p : processes) {
            processList.add(p.clone());
        }
        
        // Sort by arrival time
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        ArrayList<GanttChartEntry> ganttChart = new ArrayList<>();
        Map<String, Integer> waitingTimes = new HashMap<>();
        Map<String, Integer> turnaroundTimes = new HashMap<>();
        Map<String, Integer> completionTimes = new HashMap<>();
        
        int currentTime = 0;
        int remainingProcesses = processList.size();
        
        Queue<Process> readyQueue = new LinkedList<>();
        Process[] processArray = processList.toArray(new Process[0]);
        int nextProcess = 0;
        
        while (remainingProcesses > 0) {
            // Add newly arrived processes to the ready queue
            while (nextProcess < processArray.length && 
                   processArray[nextProcess].getArrivalTime() <= currentTime) {
                readyQueue.add(processArray[nextProcess]);
                nextProcess++;
            }
            
            if (readyQueue.isEmpty()) {
                // No process in ready queue, move time to next arrival
                if (nextProcess < processArray.length) {
                    currentTime = processArray[nextProcess].getArrivalTime();
                    continue;
                }
                break;
            }
            
            // Get next process from ready queue
            Process process = readyQueue.poll();
            
            // Calculate execution time for this quantum
            int executionTime = Math.min(quantum, process.getRemainingTime());
            
            int startTime = currentTime;
            int endTime = startTime + executionTime;
            
            // Execute process
            process.decreaseRemainingTime(executionTime);
            currentTime = endTime;
            
            // Add to gantt chart
            ganttChart.add(new GanttChartEntry(process.getId(), startTime, endTime));
            
            // Add newly arrived processes during this execution
            while (nextProcess < processArray.length && 
                   processArray[nextProcess].getArrivalTime() <= currentTime) {
                readyQueue.add(processArray[nextProcess]);
                nextProcess++;
            }
            
            // If process is not finished, add it back to ready queue
            if (process.getRemainingTime() > 0) {
                readyQueue.add(process);
            } else {
                // Process is complete
                completionTimes.put(process.getId(), currentTime);
                
                // Calculate turnaround time
                int turnaroundTime = currentTime - process.getArrivalTime();
                turnaroundTimes.put(process.getId(), turnaroundTime);
                
                // Calculate waiting time (turnaround time - burst time)
                int waitingTime = turnaroundTime - process.getBurstTime();
                waitingTimes.put(process.getId(), waitingTime);
                
                remainingProcesses--;
            }
        }
        
        // Calculate averages
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        for (String processId : waitingTimes.keySet()) {
            totalWaitingTime += waitingTimes.get(processId);
            totalTurnaroundTime += turnaroundTimes.get(processId);
        }
        
        double avgWaitingTime = totalWaitingTime / waitingTimes.size();
        double avgTurnaroundTime = totalTurnaroundTime / turnaroundTimes.size();
        
        return new SchedulingResult(ganttChart, waitingTimes, turnaroundTimes, 
                                  avgWaitingTime, avgTurnaroundTime);
    }
}