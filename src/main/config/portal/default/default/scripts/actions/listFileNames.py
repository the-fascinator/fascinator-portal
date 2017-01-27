from java.lang import System
from com.googlecode.fascinator.common import JsonSimple
from java.io import File
from org.apache.commons.io import FileUtils
import fnmatch
import os

class ListFileNamesData:

    def __init__(self):
        pass

    def __activate__(self, context):
        self.velocityContext = context
        self.writer = self.vc("response").getPrintWriter("text/html; charset=UTF-8")
        self.log = context["log"]

        if (self.vc("page").authentication.is_logged_in() and self.vc("page").authentication.is_admin()):
            self.process()
        else:
            self.throw_error("Only administrative users can access this feature")

    # Get from velocity context
    def vc(self, index):
        if self.velocityContext[index] is not None:
            return self.velocityContext[index]
        else:
            self.log.error("ERROR: Requested context entry '" + index + "' doesn't exist")
            return None

    def process(self):
        valid = self.vc("page").csrfSecurePage()
        if not valid:
            self.throw_error("Invalid request")
            return

        source = self.vc("formData").get("source")
        path = self.vc("formData").get("path")

        switch = {
            "logs" : self.show_logs
        }
        result = switch.get(source, self.unknown_action)()
        if path is not None:
            if result.containsKey(path):
                result = self.get_file_contents_as_json(path)
            else:
                self.log.info("requested log: %s but the path does not exist" % path)
                result = JsonSimple().getJsonObject()
        self.writer.println(result.toString())
        self.writer.close()

    def show_logs(self):
        rootPath = System.getProperty("fascinator.home") + "/logs"
        pattern = "*.log"
        json = self.get_file_list_as_json(rootPath, pattern)
        self.log.debug("converted to json: %s" % json)
        return json

    def get_file_list_as_json(self, rootPath, pattern):
        json = JsonSimple().getJsonObject()
        for root, dirs, files in os.walk(rootPath):
            for filename in fnmatch.filter(files, pattern):
                match = os.path.join(root, filename)
                self.log.debug("appending %s to file list" % match)
                json.put(match, filename)
        return json

    def get_file_contents_as_json(self, path):
        json = JsonSimple().getJsonObject()
        json.put(path, self.get_file_contents(path))
        return json

    def get_file_contents(self, path):
        return FileUtils.readFileToString(File(path))

    def throw_error(self, message):
        self.vc("response").setStatus(500)
        self.writer.println("Error: " + message)
        self.writer.close()

    def unknown_action(self):
        self.throw_error("Unknown source requested - '" + self.vc("formData").get("source") + "'")