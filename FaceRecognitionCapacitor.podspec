require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name = 'FaceRecognitionCapacitor'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = package['homepage']
  s.author = package['author']
  s.source = { :git => 'https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Cordova.git', :tag => s.version.to_s }
  s.source_files = 'ios/Sources/FaceRecognitionSdkPlugin/**/*.{h,m,mm,swift}'
  s.public_header_files = 'ios/Sources/FaceRecognitionSdkPlugin/**/*.h'
  s.ios.deployment_target = '13.0'
  s.dependency 'Capacitor'
  s.swift_version = '5.1'
  s.libraries = 'c++'
  s.frameworks = 'UIKit', 'Foundation', 'AVFoundation'
  # Device arm64 frameworks — do not force-link on simulator.
  s.preserve_paths = 'ios/Frameworks/**/*'
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'CLANG_CXX_LANGUAGE_STANDARD' => 'c++17',
    'FRAMEWORK_SEARCH_PATHS[sdk=iphoneos*]' => '$(inherited) "$(PODS_TARGET_SRCROOT)/ios/Frameworks"',
    'OTHER_LDFLAGS[sdk=iphoneos*]' => '$(inherited) -framework facerecognitionsdk -framework FaceRecognitionEngine -framework onnxruntime -lc++',
  }
end
