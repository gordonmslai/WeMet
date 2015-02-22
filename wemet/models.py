import random, string, util
import database

# Return a random alphanumerical id
def random_id():
  rid = ''
  for x in range(8): rid += random.choice(string.ascii_letters + string.digits)
  return rid

class Meeting(object):
    LOC_THRESHOLD = .01
    TIME_THRESHOLD = 50000 #millis

    def __init__(self, username, latitude, longitude, time):
        self.username = username
        self.latitude = latitude
        self.longitude = longitude
        self.time = time

    # {u'category': u'kklin.weartest2', u'from':
    # u'APA91bHXQNc6Co2ZEiduwIRVsGtmmmXWkIPU54vNmi_wchRPAefHuIaPTHTpI6MRHlx0h3KonwOw2MuyrXoD5lZdjIYBVna04DAAB-ajKvi0SvHXiCLTcyP7mpOsFzk0fX9oSchMlRmIH1H1iFYsn4MrFAugHT6N9Q',
    # u'time_to_live': 86400, u'data': {u'Latitude': u'37.874864', u'user_id':
    # u'1', u'Longitutde': u'-122.258213'}, u'message_id': u'5'}

    @staticmethod
    def from_xmpp_request(req):
        username = req['data']['username']
        latitude = float(req['data']['latitude'])
        longitude = float(req['data']['longitude'])
        time = int(req['data']['time'])
        return Meeting(username, latitude, longitude, time)

    # returns True if these two meetings could have been the same one
    def close(self, other):
        # TODO: how do you split statements onto multiple lines...
        return abs(self.latitude - other.latitude) < Meeting.LOC_THRESHOLD and abs(self.longitude - other.longitude) < Meeting.LOC_THRESHOLD and abs(self.time - other.time) < Meeting.TIME_THRESHOLD and self.username != other.username

    def __str__(self):
        return "[ username: {0}, latitude: {1}, longitude: {2}, time: {3} ]".format(self.username, self.latitude, self.longitude, self.time)

class MeetingResponse(object):

    def __init__(self, valid, other=None):
        self.valid = valid
        self.other = other

    def to_xmpp(self, to):
        return {'to': to,
                'message_id': random_id(),
                'data': {
                        'type': 'meeting_response',
                        'valid': str(self.valid),
                        'other': str(self.other)
                        }
                }




class Profile(object):

    EMAIL_TLD = "wemet-demo.me"

    def __init__(self, firstname, lastname, image, username, matches, real_email):
        self.firstname = firstname
        self.lastname = lastname
        self.image = image
        self.anon_email = username + "@" + Profile.EMAIL_TLD
        self.real_email = real_email
        self.username = username
        self.matches = matches

    # this generates the email alias too
    @staticmethod
    def new_profile_from_xmpp(msg):
        username = random_id()
        real_email = msg['data']['real_email']
        photo = msg['data']['photo']
        firstname = msg['data']['firstname']
        lastname = msg['data']['lastname']
        profile = Profile(firstname, lastname, photo, username, [], real_email)
        database.set_profile(profile.username, profile)
        util.generate_email_mapping(real=real_email, anon=profile.anon_email)
        return profile

    # this generates the email alias too
    @staticmethod
    def new_profile_from_json(msg):
        username = random_id()
        real_email = msg['real_email']
        photo = msg['photo']
        firstname = msg['firstname']
        lastname = msg['lastname']
        profile = Profile(firstname, lastname, photo, username, [], real_email)
        database.set_profile(profile.username, profile)
        util.generate_email_mapping(real=real_email, anon=profile.anon_email)
        return profile

    @staticmethod
    def from_json(json):
        if type(json) == unicode:
            return json
        firstname = json['firstname']
        lastname = json['lastname']
        image = json['image']
        username = json['username']
        real_email = json['real_email']
        matches = map(Profile.from_json, json['matches'])
        return Profile(firstname, lastname, image, username, matches, real_email)

    def to_xmpp(self, to):
        return {'to': to,
                'message_id': random_id(),
                'data': {
                        'type': 'get_profile_response',
                        'firstname': self.firstname,
                        'lastname': self.lastname,
                        'image': self.image,
                        'username': self.username,
                        'real_email': self.real_email,
                        'matches': self.matches
                        }
                }

    def to_json_dont_expand_matches(self):
        return {'firstname': self.firstname,
                'lastname': self.lastname,
                'image': self.image,
                'username': self.username,
                'real_email': self.real_email,
                'matches': map(lambda x: x if type(x) == unicode else x.username, self.matches)}

    def to_json(self):
        return {'firstname': self.firstname,
                'lastname': self.lastname,
                'image': self.image,
                'username': self.username,
                'real_email': self.real_email,
                'matches': map(lambda x: x.to_json_dont_expand_matches(), self.matches) }

    def add_match(self, other):
        print("Adding match between " + self.username + " and " + other.username)
        self.matches.append(other)
        database.set_profile(self.username, self)
