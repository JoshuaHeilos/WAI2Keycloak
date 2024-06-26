from flask import Flask, Response
import psycopg2
import pandas as pd
import io
#DB selecting default max connections
# http://127.0.0.1:5000/download_csv
# source ~/venv/bin/activate
# python download.py

app = Flask(__name__)

# Database connection details
DB_HOST = "localhost"
DB_PORT = "5433"
DB_NAME = "mydatabase"
DB_USER = "postgres"
DB_PASS = "secret"

def query_to_csv():
    # Connect to the PostgreSQL database
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    query = "SELECT * FROM public.users ORDER BY user_id ASC"  # Your SQL query

    # Execute the query and fetch the data into a pandas DataFrame
    df = pd.read_sql_query(query, conn)

    # Shuffle the DataFrame, keeping the header intact
    header = df.columns
    df_shuffled = df.sample(frac=1).reset_index(drop=True)
    df_shuffled.columns = header

    # Convert shuffled DataFrame to CSV
    csv_buffer = io.StringIO()
    df_shuffled.to_csv(csv_buffer, index=False)
    csv_buffer.seek(0)

    # Save the shuffled CSV to a file
    with open("shuffled_users.csv", "w") as f:
        f.write(csv_buffer.getvalue())

    conn.close()

    return csv_buffer.getvalue()

@app.route('/download_csv')
def download_csv():
    csv_data = query_to_csv()
    return Response(
        csv_data,
        mimetype="text/csv",
        headers={"Content-Disposition": "attachment;filename=shuffled_users.csv"}
    )

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=True)

