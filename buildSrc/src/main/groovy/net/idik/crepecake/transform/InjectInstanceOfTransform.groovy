package net.idik.crepecake.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import javassist.ClassPool
import net.idik.crepecake.injector.InstanceOfInjector

class InjectInstanceOfTransform extends Transform {

    String androidPath

    @Override
    String getName() {
        return InjectInstanceOfTransform.getSimpleName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        ClassPool.getDefault().insertClassPath(androidPath)
        transformInvocation.inputs.each { input ->
            input.jarInputs.each { jarInput ->
                ClassPool.getDefault().appendClassPath(jarInput.file.absolutePath)
            }
            input.directoryInputs.each { directoryInput ->
                ClassPool.getDefault().appendClassPath(directoryInput.file.absolutePath)
            }
        }
        def injector = new InstanceOfInjector()
        transformInvocation.inputs.each { input ->
            input.jarInputs.each { jarInput ->
                def dest = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
            input.directoryInputs.each { directoryInput ->
                injector.inject(directoryInput.file.absolutePath)
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }


    }

}