#!/usr/bin/python
import sys, json, xmpp, random, string, database
from threading import Lock
from models import Meeting, MeetingResponse

SERVER = 'gcm.googleapis.com'
PORT = 5235
USERNAME = "727552317391"
PASSWORD = "AIzaSyCutvGxT2Zv1Ijo3ecmESLYv1p9rKEcMFA"
REGISTRATION_ID = "APA91bGOhPZVbIu2Z7lovnZdveomqtTfbMiEmDwm1S4wZJKVU1poNmh8dyesVGsYpUkBVhL7nINcAy3jRYrI-cRyXHsF3twQcDM3D_RvsauWIxHrKNGXeSXUb9C1reLQ-jTX8VxKO2k_-RAwDZyU9HqWHiQ7lZgWxA"

unacked_messages_quota = 100
send_queue = []
usernames_to_regids = {}
meetings = []
lock = Lock()

# Return a random alphanumerical id
def random_id():
  rid = ''
  for x in range(8): rid += random.choice(string.ascii_letters + string.digits)
  return rid

# this callback should lock, so that we don't have race conditions in variables like meetings and the db
def message_callback(session, message):
  lock.acquire()
  try:
      global unacked_messages_quota
      global meetings
      global username_to_regids
      gcm = message.getTags('gcm')
      if gcm:
        gcm_json = gcm[0].getData()
        msg = json.loads(gcm_json)
        if not msg.has_key('message_type'):
          # Acknowledge the incoming message immediately.
          send({'to': msg['from'],
                'message_type': 'ack',
                'message_id': msg['message_id']})

          if msg['data']['type'] == 'meeting':
              meeting = Meeting.from_xmpp_request(msg)
              if meeting.username not in usernames_to_regids:
                usernames_to_regids[meeting.username] = msg['from']
              candidate_partners = [pot_meeting for pot_meeting in meetings if meeting.close(pot_meeting)]
              # new_meetings = meetings + [meeting]
              # print(map(str, new_meetings))
              # meetings = new_meetings
              meetings.append(meeting)
              print(map(str, meetings))
              meeting_response = MeetingResponse(False)
              if candidate_partners:
                  meeting_response = MeetingResponse(True, candidate_partners[0].username)
                  # gotta notify the first guy on the list too
                  other_meeting_response = MeetingResponse(True, meeting.username)
                  send_queue.append(other_meeting_response.to_xmpp(usernames_to_regids[candidate_partners[0].username]))
                  # TODO: update the matches variable
                  profile_a = profile_from_username(candidate_partners[0].username)
                  profile_b = profile_from_username(meeting.username)
                  profile_a.add_match(profile_b)
                  profile_b.add_match(profile_a)
              send_queue.append(meeting_response.to_xmpp(msg['from']))
          elif msg['data']['type'] == 'register':
              usernames_to_regids[msg['data']['username']] = msg['from']
          elif msg['data']['type'] == 'get_profile':
              username = msg['data']['username']
              profile = profile_from_username(username)
              send_queue.append(profile.to_xmpp(msg['from']))
          elif msg['data']['type'] == 'new_profile':
              profile = Profile.new_profile_from_xmpp(msg)
              send_qeueue.append({'to': msg['from'],
                    'message_type': 'ack',
                    'data': { 'username': profile.username }  })

        elif msg['message_type'] == 'ack' or msg['message_type'] == 'nack':
          unacked_messages_quota += 1
  finally:
    lock.release()

def profile_from_username(username):
    print("Attempting to retrieve profile for:" + username)
    return database.get_profile(username)

def send(json_dict):
  template = ("<message><gcm xmlns='google:mobile:data'>{1}</gcm></message>")
  client.send(xmpp.protocol.Message(
      node=template.format(client.Bind.bound[0], json.dumps(json_dict))))

def flush_queued_messages():
  global unacked_messages_quota
  while len(send_queue) and unacked_messages_quota > 0:
    send(send_queue.pop(0))
    unacked_messages_quota -= 1

client = xmpp.Client('gcm.googleapis.com', debug=['socket'])
client.connect(server=(SERVER,PORT), secure=1, use_srv=False)
auth = client.auth(USERNAME, PASSWORD)
if not auth:
  print 'Authentication failed!'
  sys.exit(1)

client.RegisterHandler('message', message_callback)

# send_queue.append({'to': REGISTRATION_ID,
#                    'message_id': 'reg_id',
#                    'data': {'message_destination': 'RegId',
#                             'message_id': random_id()}})

while True:
  client.Process(1)
  flush_queued_messages()
