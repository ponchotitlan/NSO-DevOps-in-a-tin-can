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
            vars.add('NAME',access_list.name)
            for rule in access_list.rule:
                vars.add('RULE_ID',rule.id)
                vars.add('ACTION',rule.action)
                vars.add('DESTINATION',rule.destination)
                template.apply("access_list",vars)
                
class configureLineTemplate(Service):
    """
    Creation of a Line Template in the device specified
    """
    @Service.create
    def cb_create(self, tctx, root, service, proplist):
        self.log.info('Service create(service=', service._path, ')')
        template = ncs.template.Template(service)
        vars = ncs.template.Variables()
        vars.add('DEVICE',service.device)
        for line_template in service.line_template:
            vars.add('NAME',line_template.name)
            vars.add('ADDRESS_GROUP',line_template.ip_group)
            vars.add('INPUT_TRANSPORT',line_template.transport_input)
            vars.add('OUTPUT_TRANSPORT',line_template.transport_output)
            vars.add('EXEC_TIMEOUT',line_template.exec_timeout)
            vars.add('SESSION_TIMEOUT',line_template.session_timeout)
            template.apply("line_template",vars)              

# ---------------------------------------------
# COMPONENT THREAD THAT WILL BE STARTED BY NCS.
# ---------------------------------------------
class Main(ncs.application.Application):
    def setup(self):
        self.log.info('Main RUNNING')
        self.register_service('access-list-router-rfs-servicepoint', configureAccessList)
        self.register_service('line-template-vty-router-rfs-servicepoint', configureLineTemplate)

    def teardown(self):
        self.log.info('Main FINISHED')