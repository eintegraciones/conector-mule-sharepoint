
package org.mule.modules.sharepoint.internal.extension;

import static org.mule.runtime.api.meta.Category.COMMUNITY;

import org.mule.modules.sharepoint.internal.connection.provider.SharepointConnectionProvider;
import org.mule.modules.sharepoint.internal.error.SharepointErrorTypeDefinition;
import org.mule.modules.sharepoint.internal.operation.SharepointOperations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

/**
 * This is the main class of an extension, is the entry point from which
 * configurations, connection providers, operations and sources are going to be
 * declared.
 */
@Xml(prefix = "sharepoint")
@Extension(name = "Sharepoint Mulesoft 4", vendor = "nttdata", category = COMMUNITY)
@ErrorTypes(SharepointErrorTypeDefinition.class)
@ConnectionProviders(SharepointConnectionProvider.class)
@Operations(SharepointOperations.class)
@RequiresEnterpriseLicense(allowEvaluationLicense = true)
public class SharepointExtension {

}

