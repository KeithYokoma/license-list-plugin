package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.google.common.annotations.VisibleForTesting
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.presentation.ArtifactInspector
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.Inspector
import io.github.jmatsu.license.presentation.LicenseInspector
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.license.tasks.internal.VariantAwareTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

abstract class InspectLicenseListTask
@Inject constructor(
    extension: LicenseListExtension,
    variant: ApplicationVariant
) : VariantAwareTask(extension, variant) {

    @VisibleForTesting
    internal object Executor {
        operator fun invoke(
            args: Args,
            logger: Logger
        ) {
            val disassembler = Disassembler(
                style = args.assemblyStyle,
                format = args.assemblyFormat
            )

            val artifactsText = args.assembledArtifactsFile.readText()
            val catalogText = args.assembledLicenseCatalogFile.readText()

            val recordedArtifacts = disassembler.disassembleArtifacts(artifactsText).values.flatten()
            val recordedLicenses = disassembler.disassemblePlainLicenses(catalogText)

            val inspector = Inspector(
                artifactDefinitions = recordedArtifacts,
                plainLicenses = recordedLicenses
            )

            val inspectedArtifacts = inspector.inspectArtifacts()
            val inspectedLicenses = inspector.inspectLicenses()
            val inspectedAssociations = inspector.inspectAssociations()

            inspectedArtifacts.forEach { (artifact, results) ->
                if (ArtifactInspector.Result.NoCopyrightHolders in results) {
                    logger.error("${artifact.key} does not have copyright holders. Only unlicense is allowed to have no copyright holders.")
                }
                if (ArtifactInspector.Result.InactiveLicense in results) {
                    logger.error("${artifact.key} has no licenses or contain *undetermined* license. Use unlicense if this has no license and/or policy.")
                }
                if (ArtifactInspector.Result.NoUrl in results) {
                    logger.error("${artifact.key} does not have url. Use `none` if no project url needs to be displayed.")
                }
            }

            inspectedLicenses.forEach { (license, results) ->
                if (LicenseInspector.Result.NoUrl in results) {
                    logger.error("${license.key} does not have url. Use `none` if no license url needs to be displayed.")
                }
                if (LicenseInspector.Result.NoName in results) {
                    logger.error("${license.key} does not have name. Use proper display name.")
                }
            }

            inspectedAssociations.missingKeys.forEach {
                logger.error("$it is required but not found in your catalog")
            }

            inspectedAssociations.restKeys.forEach {
                logger.warn("$it can be removed from your catalog")
            }
        }
    }

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        Executor(
            args = args,
            logger = logger
        )
    }

    class Args(
        project: Project,
        extension: LicenseListExtension,
        variant: ApplicationVariant
    ) : ReadWriteLicenseTaskArgs(
        project = project,
        extension = extension,
        variant = variant
    )
}
