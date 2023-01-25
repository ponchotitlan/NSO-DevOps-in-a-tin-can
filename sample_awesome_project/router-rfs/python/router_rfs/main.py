# -*- mode: python; python-indent: 4 -*-
import ncs
from ncs.application import Service


# ------------------------
# SERVICE CALLBACK EXAMPLE
# ------------------------
class configureAccessList(Service):
    """
    Creation of an Access List in the device specified
    """
    @Service.create
    def cb_create(self, tctx, root, service, proplist):
        self.log.info('Service create(service=', service._path, ')')
        template = ncs.template.Template(service)
        vars = ncs.template.Variables()
        vars.add('DEVICE',service.device)
        for access_list in service.access_list:
            vars.add('ID',access_list.id)
            vars.add('ACTION',access_list.action)
            vars.add('DESTINATION',access_list.destination)
            template.apply("access_list",vars)

# ---------------------------------------------
# COMPONENT THREAD THAT WILL BE STARTED BY NCS.
# ---------------------------------------------
class Main(ncs.application.Application):
    def setup(self):
        self.log.info('Main RUNNING')
        self.register_service('access-list-router-rfs-servicepoint', configureAccessList)

    def teardown(self):
        self.log.info('Main FINISHED')