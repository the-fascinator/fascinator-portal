from com.googlecode.fascinator.api.indexer import SearchRequest
from com.googlecode.fascinator.common.solr import SolrResult
from java.io import ByteArrayInputStream, ByteArrayOutputStream

class MaintenanceData:
    def __init__(self):
        pass

    def __activate__(self, context):
        self.velocityContext = context