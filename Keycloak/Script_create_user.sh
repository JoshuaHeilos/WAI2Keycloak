#!/bin/bash

#EXAMPLE USE
#./Script_create_user.sh 0
#!/bin/bash

# Path to the home venv
HOME_VENV="$HOME/venv"

# Check if the venv exists
if [ ! -d "$HOME_VENV" ]; then
  # Create the virtual environment if it doesn't exist
  python3 -m venv "$HOME_VENV"
fi

# Activate the home virtual environment
source "$HOME_VENV/bin/activate"

# Ensure all needed modules are installed
pip install requests

# Set environment variable for DELETE_USERS_FLAG based on argument
if [ "$1" == "1" ]; then
  export DELETE_USERS_FLAG=1
else
  export DELETE_USERS_FLAG=0
fi

# Start the performance monitoring script
python3 << EOF

import os
import requests
import json
import random
import csv

# Keycloak configuration
KEYCLOAK_URL = "http://localhost:8080"
REALM_NAME = "testrealm"
CLIENT_ID = "create"
CLIENT_SECRET = "r8HJr35blPwHJOSwHRuXOvoqmIry4wus"

# Get the DELETE_USERS flag from the environment variable
DELETE_USERS = os.getenv('DELETE_USERS_FLAG') == '1'

# Lists for random data generation
FIRST_NAMES = [
    "Emma", "Liam", "Olivia", "Noah", "Ava", "William", "Sophia", "James", "Isabella", 
    "Benjamin", "Mia", "Lucas", "Charlotte", "Mason", "Amelia", "Ethan", "Harper", 
    "Alexander", "Evelyn", "Henry", "Abigail", "Sebastian", "Ella", "Jackson", 
    "Elizabeth", "Aiden", "Sofia", "Matthew", "Avery", "Samuel", "Scarlett", 
    "David", "Grace", "Joseph", "Chloe", "Carter", "Victoria", "Owen", "Riley", 
    "Wyatt", "Aria", "John", "Lily", "Jack", "Nora", "Luke", "Hazel", "Daniel", 
    "Zoe", "Gabriel", "Hannah"
]
LAST_NAMES = [
    "Smith", "Johnson", "Brown", "Taylor", "Miller", "Wilson", "Moore", "Anderson", 
    "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", 
    "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen", 
    "Young", "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott", "Green", 
    "Adams", "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", 
    "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins", 
    "Stewart", "Sanchez", "Morris"
]
COMPANIES = {
    1: "Google",
    2: "Microsoft",
    3: "Apple",
    4: "Amazon",
    5: "IBM",
    6: "Facebook",
    7: "Twitter",
    8: "LinkedIn",
    9: "Netflix",
    10: "Tesla",
    11: "Samsung",
    12: "Intel",
    13: "Oracle",
    14: "Adobe",
    15: "Salesforce",
    16: "Cisco",
    17: "HP",
    18: "Sony",
    19: "Dell",
    20: "Nvidia"
}

ROLES = ["Employee", "Employee", "Employee", "TeamLeader"]  # 75% Employee, 25% TeamLeader

def get_access_token():
    try:
        url = f"{KEYCLOAK_URL}/realms/{REALM_NAME}/protocol/openid-connect/token"
        data = {
            "grant_type": "client_credentials",
            "client_id": CLIENT_ID,
            "client_secret": CLIENT_SECRET
        }
        print(f"Sending POST request to URL: {url}")
        response = requests.post(url, data=data)
        if response.status_code == 200:
            return response.json()["access_token"]
        else:
            print(f"Failed to get access token. Status code: {response.status_code}")
            return None
    except Exception as e:
        print(f"An error occurred in get_access_token: {e}")
        return None


def delete_all_users(access_token):
    if not access_token:
        print("Access token is not available. Cannot delete users.")
        return False

    try:
        base_url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/users"
        headers = {"Authorization": f"Bearer {access_token}"}

        while True:
            response = requests.get(base_url, headers=headers)  
            response.raise_for_status()

            users = response.json()
            if not users:
                print("All users deleted successfully.")
                break

            for user in users:
                user_id = user["id"]
                delete_url = f"{base_url}/{user_id}"
                delete_response = requests.delete(delete_url, headers=headers)
                delete_response.raise_for_status()
                #print(f"Deleted user with ID: {user_id}")

        return True

    except requests.RequestException as e:
        print(f"Error deleting users: {e}")
        return False
        
def create_user(access_token, company_name, first_name, last_name, role, user_number, companyId):  
    if not access_token:
        print("Access token is not available. Cannot create user.")
        return 'Error'

    try:
        email = f"{first_name.lower()}.{last_name.lower()}.{user_number:02d}@{company_name.lower()}.com"
        username = email 
        url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/users"
        headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json"
        }
        password = "password"

        user_data = {
            "username": username,
            "email": email,
            "firstName": first_name,
            "lastName": last_name,
            "enabled": True,
            "emailVerified": True,  
            "credentials": [{"type": "password", "value": password, "temporary": False}],
            "realmRoles": [role],
            "attributes": {"companyId": str(companyId)}
        }

        response = requests.post(url, headers=headers, json=user_data)
        if response.status_code == 201:
            return response.headers["Location"].split("/")[-1]  
        elif response.status_code == 409:
            print(f"User with username '{username}' already exists. Skipping creation.")
        else:
            print(f"Failed to create user: {response.text}")
        return None
    except Exception as e:
        print(f"An error occurred in create_user: {e}")
        return 'Error'

def assign_role_to_user(access_token, user_id, role_name):
    if not access_token:
        print("Access token is not available. Cannot assign role.")
        return False

    try:
        url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/users/{user_id}/role-mappings/realm"
        headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json"
        }

        role_data = [{
            "id": get_role_id(access_token, role_name),
            "name": role_name
        }]

        response = requests.post(url, headers=headers, json=role_data)
        if response.status_code == 204:  
            return True
        else:
            print(f"Failed to assign role '{role_name}' to user ID {user_id}: {response.text}")
            return False
    except Exception as e:
        print(f"An error occurred while assigning role: {e}")
        return False


def get_role_id(access_token, role_name):
    if not access_token:
        print("Access token is not available. Cannot fetch role ID.")
        return None
    try:
        url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/roles"
        headers = {
            "Authorization": f"Bearer {access_token}",
        }
        response = requests.get(url, headers=headers)
        response.raise_for_status()

        roles = response.json()
        for role in roles:
            if role["name"] == role_name:
                return role["id"]
        print(f"Error: Role '{role_name}' not found.")
        return None
    except requests.RequestException as e:
        print(f"Error getting role ID: {e}")
        return None


def main():
    try:
        access_token = get_access_token()
        if not access_token:
            print("Failed to get access token. Exiting.")
            return

        if DELETE_USERS:
            print("Delete All users.")
            delete_all_users(access_token)
        else:
            print("Create Users.")
            users = []
            for companyId, company_name in COMPANIES.items():
                for _ in range(100):  
                    first_name = random.choice(FIRST_NAMES)
                    last_name = random.choice(LAST_NAMES)
                    user_number = random.randint(1, 50)
                    email = f"{first_name.lower()}.{last_name.lower()}.{user_number:02d}@{company_name.lower()}.com"
                    username = email
                    role = random.choice(ROLES)

                    user_creation_result = create_user(access_token, company_name, first_name, last_name, role, user_number, companyId)

                    if user_creation_result != 'Error':
                        user_id = user_creation_result
                        if user_id:
                            assign_role_to_user(access_token, user_id, role) 

                            users.append({
                                "userId": user_id,
                                "name": f"{first_name} {last_name}",
                                "email": email,
                                "role": role,
                                "company": company_name,
                                "companyId": companyId,
                                "password": "password",
                            })

            output_dir = os.path.join(os.getenv('HOME'), 'wai2_website_minimal', 'backend')
            os.makedirs(output_dir, exist_ok=True)
            output_file = os.path.join(output_dir, 'keycloak_users.csv')

            with open(output_file, 'w', newline='') as csvfile:
                fieldnames = ['userId', 'name', 'email', 'role', 'company', 'companyId', 'password']  
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                for user in users:
                    writer.writerow(user)

            print(f"Created {len(users)} users and saved to {output_file}")

    except Exception as e:
        print(f"An error occurred in main: {e}")

if __name__ == "__main__":
    main()
EOF

