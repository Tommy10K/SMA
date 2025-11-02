from flask import Flask, request, jsonify
from twilio.rest import Client
import json
import os

app = Flask(__name__)

DATA_FILE = "data.json"

ACCOUNT_SID = ""
AUTH_TOKEN = ""
TWILIO_PHONE_NUMBER = ""

try:
    client = Client(ACCOUNT_SID, AUTH_TOKEN)
    print("Twilio client initialized successfully.")
except Exception as e:
    print(f"Error initializing Twilio client: {e}")
    client = None

def load_data():
    """Load topics and subscribers from JSON file."""
    if not os.path.exists(DATA_FILE):
        return {"topics": []}
    with open(DATA_FILE, "r") as f:
        return json.load(f)

def save_data(data):
    """Save topics and subscribers to JSON file."""
    with open(DATA_FILE, "w") as f:
        json.dump(data, f, indent=4)

def find_topic(data, topic_name):
    """Return the topic dict matching topic_name, or None."""
    for topic in data["topics"]:
        if topic["name"] == topic_name:
            return topic
    return None

@app.route('/')
def index():
    return "Backend running: topics & subscriptions persistent."

@app.route("/topics", methods=["GET"])
def get_topics():
    """Return all available topics."""
    data = load_data()
    return jsonify({"topics": [t["name"] for t in data["topics"]]})

@app.route("/topics", methods=["POST"])
def create_topic():
    """Create a new topic."""
    new_topic_name = request.json.get("name")
    if not new_topic_name:
        return jsonify({"error": "Missing 'name' field"}), 400

    data = load_data()
    if find_topic(data, new_topic_name):
        return jsonify({"error": "Topic already exists"}), 400

    topic = {"name": new_topic_name, "subscribers": []}
    data["topics"].append(topic)
    save_data(data)
    print(f"Created new topic: {new_topic_name}")
    return jsonify({"message": "Topic created", "topic": topic}), 201

@app.route("/topics/<topic_name>/subscribe", methods=["POST"])
def subscribe(topic_name):
    """Subscribe a phone number to a topic."""
    phone = request.json.get("phone")
    if not phone:
        return jsonify({"error": "Missing 'phone' field"}), 400

    data = load_data()
    topic = find_topic(data, topic_name)
    if not topic:
        return jsonify({"error": f"Topic '{topic_name}' not found"}), 404

    if phone not in topic["subscribers"]:
        topic["subscribers"].append(phone)
        save_data(data)
        print(f"{phone} subscribed to {topic_name}")

    return jsonify({"message": f"Subscribed {phone} to {topic_name}"}), 200

@app.route("/topics/<topic_name>/subscribe", methods=["DELETE"])
def unsubscribe(topic_name):
    phone = request.json.get("phone")
    if not phone:
        return jsonify({"error": "Missing 'phone' field"}), 400

    data = load_data()
    topic = find_topic(data, topic_name)
    if not topic:
        return jsonify({"error": f"Topic '{topic_name}' not found"}), 404

    if phone in topic["subscribers"]:
        topic["subscribers"].remove(phone)
        save_data(data)
        print(f"{phone} unsubscribed from {topic_name}")
        return jsonify({"message": f"Unsubscribed {phone} from {topic_name}"}), 200
    else:
        return jsonify({"message": f"{phone} was not subscribed to {topic_name}"}), 200

@app.route("/subscriptions/<phone_number>", methods=["GET"])
def get_user_subscriptions(phone_number):

    data = load_data()
    my_subs = []
    for topic in data["topics"]:
        if phone_number in topic["subscribers"]:
            my_subs.append(topic["name"])

    return jsonify({"phone": phone_number, "subscriptions": my_subs}), 200

@app.route("/topics/<topic_name>/publish", methods=["POST"])
def publish(topic_name):
    """Send a message to all subscribers of a topic via SMS."""
    if not client:
        return jsonify({"error": "Twilio client not initialized"}), 500

    title = request.json.get("title")
    message = request.json.get("message")
    if not title or not message:
        return jsonify({"error": "Missing 'title' or 'message'"}), 400

    data = load_data()
    topic = find_topic(data, topic_name)
    if not topic:
        return jsonify({"error": f"Topic '{topic_name}' not found"}), 404

    subscribers = topic["subscribers"]
    if not subscribers:
        return jsonify({"status": "no subscribers", "topic": topic_name})

    print(f"\nBroadcasting to topic '{topic_name}':")
    sent_to = []
    failed_for = []

    sms_body = f"[{title}] {message}"

    for phone in subscribers:
        try:
            message = client.messages.create(
                body=sms_body,
                from_=TWILIO_PHONE_NUMBER,
                to=phone
            )
            print(f"  → Successfully sent SMS to {phone} (SID: {message.sid})")
            sent_to.append(phone)
        except Exception as e:
            print(f"  → FAILED to send SMS to {phone}: {e}")
            failed_for.append(phone)

    return jsonify({
        "status": "broadcast complete",
        "topic": topic_name,
        "sent_to": sent_to,
        "failed_for": failed_for
    }), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080)
