#!/bin/bash

# Function to clean up background processes
cleanup() {
    echo "Cleaning up..."
    if [ -n "$monitor_pid" ]; then
        kill $monitor_pid 2>/dev/null
    fi
    if [ "$VISUALVM" -eq 1 ] && [ -n "$visualvm_pid" ]; then
        kill $visualvm_pid 2>/dev/null
    fi
}

# Set trap to catch termination signals and clean up
trap cleanup SIGINT SIGTERM EXIT

# Navigate to the JMeter directory
cd "$(dirname "$0")"
#EXAMPLE
#THREADS=50 RAMP_UP=20 DURATION=240 TEST=Keycloak VISUALVM=1 ./run_test.sh
# Default values for JMeter test parameters
THREADS=${THREADS:-20}
RAMP_UP=${RAMP_UP:-5}
DURATION=${DURATION:-60}
VISUALVM=${VISUALVM:-0}  # Default is not to start VisualVM

# Add some buffer time for JMeter to finish
DURATION=$((DURATION + 20))

# Base directories and files (using HOME_DIR if it's set)
HOME_DIR=${HOME_DIR:-$HOME}

# Check the TEST environment variable and set TEST_PLAN and RESULT_BASE_DIR accordingly
if [ "$TEST" == "Session" ]; then
  TEST_PLAN="$HOME_DIR/wai2_website_minimal/JMeter/SessionTestPlan/TestPlanComp_Session.jmx"
  RESULT_BASE_DIR="$HOME_DIR/wai2_website_minimal/JMeter/SessionTestPlan/Result_${THREADS}"
elif [ "$TEST" == "Keycloak" ]; then
  TEST_PLAN="$HOME_DIR/wai2_website_minimal/JMeter/KeyCloakTestPlan/TestPlanComp_Keycloak.jmx"
  RESULT_BASE_DIR="$HOME_DIR/wai2_website_minimal/JMeter/KeyCloakTestPlan/Result_${THREADS}"
else
  echo "Unknown test type: $TEST. Please set the TEST environment variable to either 'SESSION' or 'Keycloak'."
  exit 1
fi

# Print out the selected test plan and result directory for verification
echo "Using TEST_PLAN: $TEST_PLAN"
echo "Using RESULT_BASE_DIR: $RESULT_BASE_DIR"

RESULT_FILE="result.jtl"
RUN_NUMBER=1

while [ -d "${RESULT_BASE_DIR}_${RUN_NUMBER}" ]; do
    RUN_NUMBER=$((RUN_NUMBER + 1))
done

# Result directory for this run
RESULT_DIR="${RESULT_BASE_DIR}_${RUN_NUMBER}"
FILTERED_RESULTS="$RESULT_DIR/Filtered_Result/filtered_results.csv"

# Create the result directories
mkdir -p "$RESULT_DIR/Graphs"
mkdir -p "$RESULT_DIR/Result"
mkdir -p "$RESULT_DIR/Log"
mkdir -p "$RESULT_DIR/Performance"
mkdir -p "$RESULT_DIR/Graphs_Filtered"

# Set the heap size to 4GB and define the GC algorithm options
export HEAP="-Xms1g -Xmx4g"
export GC_ALGO="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Xlog:gc=error:file=$RESULT_DIR/Log/gc.log"

# Define the JMeter command with additional parameters
JMETER_CMD="$HOME/apache-jmeter-5.5/bin/jmeter -t $TEST_PLAN -n -l $RESULT_DIR/Result/${RESULT_FILE} -j $RESULT_DIR/Log/jmeter.log -e -o ${RESULT_DIR}/Graphs -Jhome_dir=${HOME_DIR} -Jresult_dir=${RESULT_DIR} -Jrun_number=${RUN_NUMBER} -Jthreads=${THREADS} -Jramp_up=${RAMP_UP} -Jduration=${DURATION}"

# Print the JMeter command to the console
echo "JMeter command: $JMETER_CMD"

# Check if the directories were created successfully
echo "Result directory: $RESULT_DIR"
ls -l $RESULT_DIR
echo "Result file path: $RESULT_DIR/Result/${RESULT_FILE}"

# Create a virtual environment and install necessary packages
python3 -m venv venv
source venv/bin/activate
pip install psutil matplotlib

# Start the performance monitoring script
python3 << EOF &
import psutil
import time
import json
import os
import signal
import sys

def handle_sigterm(signum, frame):
    sys.exit(0)

signal.signal(signal.SIGTERM, handle_sigterm)

def monitor_performance(duration, interval=1):  # Reduced interval for frequent updates
    print(f"Monitoring performance for {duration} seconds")
    processes = {
        "postgres": [],
        "docker": [],
        "java": [],
        "gnome-terminal": []
    }

    # Store previous state of processes to detect changes
    previous_states = {key: [] for key in processes}

    cpu_usage = {key: [] for key in processes}
    memory_usage = {key: [] for key in processes}

    # Number of CPU cores
    cpu_cores = psutil.cpu_count(logical=True)

    start_time = time.time()
    while time.time() - start_time < duration:
        #print(f"Elapsed time: {time.time() - start_time:.2f} seconds")
        current_procs = list(psutil.process_iter(['pid', 'name', 'cmdline']))
        for key in processes.keys():
            new_pids = [p.pid for p in current_procs if p.info['name'].lower().startswith(key)]
            if set(new_pids) != set(previous_states[key]):
                processes[key] = [p for p in current_procs if p.pid in new_pids]
                print(f"Change detected in {key} processes. New set: {[p.pid for p in processes[key]]}")
                previous_states[key] = new_pids

        for key, procs in processes.items():
            cpu_total = sum(p.cpu_percent(interval=0) / cpu_cores for p in procs if p.is_running())
            memory_total = sum(p.memory_info().rss for p in procs if p.is_running())
            cpu_usage[key].append((time.time() - start_time, cpu_total))  # Use relative time
            memory_usage[key].append((time.time() - start_time, memory_total))
        time.sleep(interval)

    performance_data = {
        "cpu_usage": cpu_usage,
        "memory_usage": memory_usage,
    }

    with open("$RESULT_DIR/Performance/performance_data.json", "w") as f:
        json.dump(performance_data, f)

monitor_performance($DURATION)
EOF

# Capture the PID of the performance monitoring script
monitor_pid=$!

# Conditionally start VisualVM if VISUALVM is set to 1
if [ "$VISUALVM" -eq 1 ]; then
    $HOME/visualvm/visualvm_218/bin/visualvm &
    visualvm_pid=$!
fi

# Run JMeter
eval "$JMETER_CMD"

# Wait for the performance monitoring script to complete
wait $monitor_pid

# Check if the performance data file exists
if [ ! -f "$RESULT_DIR/Performance/performance_data.json" ]; then
    echo "An error occurred: Performance data file not found"
    exit 1
fi

# Run FilterResults.sh after JMeter completes
echo "Filtering results"
$HOME/apache-jmeter-5.5/bin/FilterResults.sh \
    --output-file "$FILTERED_RESULTS" \
    --input-file "$RESULT_DIR/Result/$RESULT_FILE" \
    --exclude-labels "JSR223 Prepare Course Book User,JSR223 Prepare Course Progress User,JSR223 Prepare Course Deletion Company,JSR223 Prepare Course Booking Company,HTTP Request: Fetch Session Info,HTTP Request: Delete Course Company"

echo "Filtering complete. Filtered results saved to: $FILTERED_RESULTS"

# Generate new report from filtered results
REPORT_GENERATOR_CMD="$HOME/apache-jmeter-5.5/bin/jmeter -g $FILTERED_RESULTS -o $RESULT_DIR/Graphs_Filtered"
echo "Generating new report from filtered results..."
eval "$REPORT_GENERATOR_CMD"
echo "New report generated in: $RESULT_DIR/Graphs_Filtered"

# Draw graphs of the performance metrics
echo "Drawing graphs of the performance metrics..."

# Use python to generate graphs from the exported data
python3 << EOF
import json
import matplotlib.pyplot as plt

# Load the performance data
with open("${RESULT_DIR}/Performance/performance_data.json", 'r') as f:
    performance_data = json.load(f)

def plot_data(data, title, ylabel, filename):
    plt.figure(figsize=(10, 5))
    for key in data.keys():
        timestamps = [item[0] for item in data[key]]
        values = [item[1] for item in data[key]]
        plt.plot(timestamps, values, label=key)
    plt.xlabel('Time (s)')
    plt.ylabel(ylabel)
    plt.title(title)
    plt.legend()
    plt.savefig(f'{filename}.png')

for key in performance_data["cpu_usage"].keys():
    plot_data({key: performance_data["cpu_usage"][key]}, f'{key.capitalize()} CPU Usage', 'CPU Usage (%)', f'${RESULT_DIR}/Performance/{key}_cpu_usage')

for key in performance_data["memory_usage"].keys():
    plot_data({key: performance_data["memory_usage"][key]}, f'{key.capitalize()} Memory Usage', 'Memory Usage (bytes)', f'${RESULT_DIR}/Performance/{key}_memory_usage')

print("Performance graphs saved successfully.")
EOF

echo "Performance metrics exported and graphs generated successfully."

# Conditionally kill VisualVM if it was started
if [ "$VISUALVM" -eq 1 ]; then
    kill $visualvm_pid
fi

echo "Script completed successfully."

