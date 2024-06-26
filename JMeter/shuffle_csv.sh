#!/bin/bash

# Check if a filename is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <csv_file>"
    exit 1
fi

# Input CSV file
input_file="$1"

# Extract the header
header=$(head -n 1 "$input_file")

# Extract the rest of the file, shuffle it, and then combine with the header
{
    echo "$header"
    tail -n +2 "$input_file" | shuf
} > shuffled_"$input_file"

echo "Shuffled file created: shuffled_$input_file"

