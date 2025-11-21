
class Temurin17Test():

    def test_jre(self):
        Anson.java_src('semanticshare')
        release = cast(Temurin17Release, Anson.from_file('temurin17-bin.json'))
