from com.googlecode.fascinator.api.indexer import SearchRequest
from com.googlecode.fascinator.common.solr import SolrResult
from com.googlecode.fascinator.spring import ApplicationContextProvider
from java.io import ByteArrayInputStream, ByteArrayOutputStream

class MaintenanceData:
    def __init__(self):
        pass

    def __activate__(self, context):
        self.velocityContext = context
        self.response = self.velocityContext["response"]
        self.maintenanceModeService = ApplicationContextProvider.getApplicationContext().getBean("maintenanceModeService")
        if self.maintenanceModeService.isMaintanceMode() == False:
            self.response.sendRedirect(self.velocityContext["portalPath"]+"/home")
