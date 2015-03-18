#!/usr/bin/env python
import web
import database
import json
from models import Profile

urls = (
    '/create_profile/', 'create_profile',
    '/profiles/(.*)', 'get_profile',
    '/all_profiles/', 'all_profiles'

)

app = web.application(urls, globals())

class create_profile:
    def POST(self):
        data = json.loads(web.data())
        profile = Profile.new_profile_from_json(data)
        return json.dumps({'username': profile.username})

class get_profile:
    def GET(self, user):
        return json.dumps(database.get_profile(user).to_json())

class all_profiles: #returns readable string of all profiles in database
    def GET(self):
        return json.dumps(database.all_profiles(), indent = 2)

if __name__ == "__main__":
    web.httpserver.runsimple(app.wsgifunc(), ("0.0.0.0", 80))
    # app.run('0.0.0.0:80')
