package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import groovy.text.SimpleTemplateEngine

class PropertyFilePropertyCountRule extends Rule {

    static final String RULE_ID = 'PROPERTY_FILE_COUNT_MISMATCH'
    static final String RULE_NAME = 'Property File Count Mismatch'
    static final String RULE_VIOLATION_MESSAGE = 'Properties files do not have matching number of properties. '
    static final String DEFAULT_PATTERN = '${appname}-${env}.properties'

    String[] environments
    String pattern

    PropertyFilePropertyCountRule(List<String> environments) {
        this(environments, DEFAULT_PATTERN)
    }

    PropertyFilePropertyCountRule(List<String> environments, String pattern) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.environments = environments
        this.pattern = pattern
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List<PropertyFile> propFiles = getValidPropertyFiles(getValidPropertyFilenames(app.name), app.propertyFiles)

        if(propFiles*.getPropertyCount().unique().size() > 1) {
            Map counts = propFiles.collectEntries { [it.name, it.getPropertyCount()]}
            propFiles.each { file ->
                violations.add(new RuleViolation(this, file.getName(), 0, RULE_VIOLATION_MESSAGE + counts))
            }
        }

        return violations
    }

    List getValidPropertyFilenames(String applicationName) {
        List validPropertyFilenames = []
        environments.each { env ->
            Map<String, String> binding = ['appname':applicationName, 'env':env]
            String fileName = new SimpleTemplateEngine().createTemplate(pattern).make(binding)
            validPropertyFilenames.add(fileName)
        }
        return validPropertyFilenames
    }

    List getValidPropertyFiles(List validPropertyFilenames, List propertyFiles) {
        List<PropertyFile> validPropertyFiles = []
        propertyFiles.each {
            if(it.getName() in validPropertyFilenames){
                validPropertyFiles.add(it)
            }
        }
        return validPropertyFiles
    }
}
