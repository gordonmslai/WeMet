# A mock database for testing
from models import Profile
import pickledb
import json

profiles = [None, None]
profiles[0] = Profile(firstname="Kevin",
                  lastname="Lin",
                  image="https://scontent-sjc.xx.fbcdn.net/hphotos-xfp1/v/l/t1.0-9/10653355_10205014838109166_5841389645200777638_n.jpg?oh=ae44ad88ce8cfbe460e4ec6fd4519d49&oe=5589699C",
                  real_email="kaikai526@gmail.com",
                  username="kklin",
                  matches=[]) # this should really be matched with profiles[1]
profiles[1] = Profile(firstname="Gordon",
                  lastname="Lai",
                  image="https://fbcdn-sphotos-f-a.akamaihd.net/hphotos-ak-xpf1/v/t1.0-9/10570353_10203107682745843_148973457800377648_n.jpg?oh=8c73d2139b21760b27cb25d2f2ece957&oe=55846B8B&__gda__=1434804916_5242d884e9c85256e68ec2596ec1c260",
                  real_email="gordonmslai@gmail.com",
                  username="gordonmslai",
                  matches=[profiles[0]])

db = pickledb.load('/home/kklin/wemet.db', False)

def set(key, value):
    db.set(key, value.to_json())
    db.set(key, value)
    db.dump()

def get(key):
    return db.get(key)

def set_profile(username, profile):
    print(profile.to_json())
    print(json.dumps(profile.to_json()))
    db.set(username, json.dumps(profile.to_json()))
    db.dump()

def get_profile(username):
    # this is so so wrong
    db = pickledb.load('/home/kklin/wemet.db', False)
    print(json.loads(db.get(username)))
    return Profile.from_json(json.loads(db.get(username)))
