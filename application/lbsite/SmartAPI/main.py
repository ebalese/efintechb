import json
import pprint   



# Path to your JSON file
file_path = "data.json"

# Open and load the JSON file
with open(file_path, "r", encoding="utf-8") as file:
    
    data = json.load(file)
    data_string = json.dumps(data)
    readable = pprint.pformat(data, indent=2)

    del data["shoppingCart"]["items"][0]

    print("After deletion:")
    new_readable = pprint.pformat(data, indent=2)
    print(new_readable)
 







    print(readable)

    print("data_string is:")
    print(data_string)
    print("-------")




    file2 = json.dump(data, open("data2.json", "w"))
    print(f"Data type: {type(data)}")
    print(f"Data string type: {type(data_string)}")

# Print the loaded data
print("Script is running")

print("Data as JSON string:")
print(data_string)

