import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/network/api_client.dart';
import '../../../core/services/location_service.dart';
import '../../../core/services/media_service.dart';
import '../../../core/utils/validators.dart';
import '../../../data/models/challenge_models.dart';
import '../../../providers/challenge_provider.dart';
import '../../../providers/domain_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';
import 'challenge_success_screen.dart';

class SubmitChallengeScreen extends StatefulWidget {
  const SubmitChallengeScreen({super.key});

  @override
  State<SubmitChallengeScreen> createState() => _SubmitChallengeScreenState();
}

class _SubmitChallengeScreenState extends State<SubmitChallengeScreen> {
  final _formKey = GlobalKey<FormState>();

  // Form Controllers
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _subCategoryController = TextEditingController();
  final _addressController = TextEditingController();
  final _districtController = TextEditingController(text: 'Varanasi');
  final _stateController = TextEditingController(text: 'Uttar Pradesh');
  final _pincodeController = TextEditingController(text: '221005');
  final _latController = TextEditingController(text: '25.2677');
  final _lngController = TextEditingController(text: '82.9913');
  final _populationController = TextEditingController(text: '1200');

  String? _selectedDomainCode;
  String _selectedSeverity = 'CRITICAL';
  String _selectedUrgency = 'IMMEDIATE';
  String _selectedJurisdiction = 'PANCHAYAT_PRI';

  // Services
  late final LocationService _locationService;
  late final MediaService _mediaService;

  bool _isFetchingLocation = false;
  bool _isUploadingMedia = false;

  // Attachments List
  final List<AttachmentDto> _attachments = [];

  @override
  void initState() {
    super.initState();
    _locationService = LocationService();
    // Media service will be instantiated with the ApiClient provider
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DomainProvider>().fetchDomains();
    });
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final apiClient = context.read<ApiClient>();
    _mediaService = MediaService(apiClient);
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _subCategoryController.dispose();
    _addressController.dispose();
    _districtController.dispose();
    _stateController.dispose();
    _pincodeController.dispose();
    _latController.dispose();
    _lngController.dispose();
    _populationController.dispose();
    super.dispose();
  }

  Future<void> _captureGpsLocation() async {
    setState(() => _isFetchingLocation = true);
    try {
      final loc = await _locationService.getCurrentLocation();
      if (loc != null) {
        setState(() {
          _latController.text = loc.latitude.toStringAsFixed(6);
          _lngController.text = loc.longitude.toStringAsFixed(6);
          if (loc.district != null && loc.district!.isNotEmpty) {
            _districtController.text = loc.district!;
          }
          if (loc.state != null && loc.state!.isNotEmpty) {
            _stateController.text = loc.state!;
          }
          if (loc.pincode != null && loc.pincode!.isNotEmpty) {
            _pincodeController.text = loc.pincode!;
          }
          if (loc.displayName != null && loc.displayName!.isNotEmpty) {
            _addressController.text = loc.displayName!;
          } else if (loc.locality != null && loc.locality!.isNotEmpty) {
            _addressController.text = loc.locality!;
          }
        });
        if (mounted) {
          final placeName = loc.district ?? loc.locality ?? 'Live GPS';
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('📍 Location detected: $placeName (${loc.latitude.toStringAsFixed(4)}, ${loc.longitude.toStringAsFixed(4)})'),
              backgroundColor: AppColors.emerald,
              behavior: SnackBarBehavior.floating,
            ),
          );
        }
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('GPS location unavailable. Using district defaults.'),
              behavior: SnackBarBehavior.floating,
            ),
          );
        }
      }
    } finally {
      if (mounted) setState(() => _isFetchingLocation = false);
    }
  }

  void _showMediaPickerSheet() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                'Add Evidence & Proof',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              ListTile(
                leading: const Icon(Icons.camera_alt_outlined, color: AppColors.primary),
                title: const Text('Take Photo with Camera'),
                onTap: () {
                  Navigator.of(ctx).pop();
                  _pickImage(ImageSource.camera);
                },
              ),
              ListTile(
                leading: const Icon(Icons.photo_library_outlined, color: AppColors.primary),
                title: const Text('Choose Photo from Gallery'),
                onTap: () {
                  Navigator.of(ctx).pop();
                  _pickImage(ImageSource.gallery);
                },
              ),
              ListTile(
                leading: const Icon(Icons.upload_file_outlined, color: AppColors.primary),
                title: const Text('Upload Document (PDF / Lab Report)'),
                onTap: () {
                  Navigator.of(ctx).pop();
                  _pickDocument();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _pickImage(ImageSource source) async {
    setState(() => _isUploadingMedia = true);
    try {
      final attachment = await _mediaService.pickAndUploadImage(
        source: source,
        caption: 'Site photo evidence captured by citizen',
      );
      if (attachment != null && mounted) {
        setState(() {
          _attachments.add(attachment);
        });
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Evidence "${attachment.fileName}" uploaded to civic cloud'),
            backgroundColor: AppColors.emerald,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Upload notice: $e'),
            backgroundColor: AppColors.ruby,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isUploadingMedia = false);
    }
  }

  Future<void> _pickDocument() async {
    setState(() => _isUploadingMedia = true);
    try {
      final attachment = await _mediaService.pickAndUploadDocument(
        caption: 'Laboratory report / official citizen petition document',
      );
      if (attachment != null && mounted) {
        setState(() {
          _attachments.add(attachment);
        });
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Document "${attachment.fileName}" uploaded successfully'),
            backgroundColor: AppColors.emerald,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Document upload notice: $e'),
            backgroundColor: AppColors.ruby,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isUploadingMedia = false);
    }
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) return;

    final lat = double.tryParse(_latController.text.trim()) ?? 25.2677;
    final lng = double.tryParse(_lngController.text.trim()) ?? 82.9913;
    final population = int.tryParse(_populationController.text.trim()) ?? 100;

    final dto = SubmitChallengeDto(
      title: _titleController.text.trim(),
      description: _descriptionController.text.trim(),
      domainCode: _selectedDomainCode,
      subCategory: _subCategoryController.text.trim().isNotEmpty ? _subCategoryController.text.trim() : null,
      submitterType: 'CITIZEN',
      latitude: lat,
      longitude: lng,
      addressLine: _addressController.text.trim().isNotEmpty ? _addressController.text.trim() : null,
      district: _districtController.text.trim(),
      state: _stateController.text.trim(),
      pincode: _pincodeController.text.trim(),
      jurisdictionLevel: _selectedJurisdiction,
      severityLevel: _selectedSeverity,
      urgencyLevel: _selectedUrgency,
      estimatedAffectedPopulation: population,
      attachments: _attachments,
    );

    final challengeProvider = context.read<ChallengeProvider>();
    final created = await challengeProvider.submitChallenge(dto);

    if (!mounted) return;

    if (created != null) {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(
          builder: (_) => ChallengeSuccessScreen(challenge: created),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(challengeProvider.submitError ?? 'Submission failed'),
          backgroundColor: AppColors.ruby,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final domainProvider = context.watch<DomainProvider>();
    final challengeProvider = context.watch<ChallengeProvider>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Report Societal Challenge'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 20),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // GovTech Guidance Header Card
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: AppColors.primary.withValues(alpha: 0.06),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.primary.withValues(alpha: 0.2)),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.info_outline_rounded, color: AppColors.primary, size: 20),
                    SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        'Your report is analyzed by server-side AI for domain classification, priority scoring, duplicate detection, and automated routing to government departments and university R&D hubs.',
                        style: TextStyle(
                          fontSize: 12,
                          color: AppColors.primary,
                          height: 1.4,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Section 1: Problem Overview
              const Text(
                '1. Problem Overview',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 12),

              CustomTextField(
                controller: _titleController,
                label: 'Problem Title',
                hintText: 'e.g. Ground Water Arsenic and Fluoride Contamination in Chandauli',
                validator: (v) => Validators.minLength(v, 5, fieldName: 'Title'),
              ),
              const SizedBox(height: 14),

              CustomTextField(
                controller: _descriptionController,
                label: 'Detailed Description & Impact',
                hintText: 'Explain the root societal challenge, how many people are affected, health/economic impacts, and what solution is needed...',
                maxLines: 4,
                validator: (v) => Validators.minLength(v, 20, fieldName: 'Description'),
              ),
              const SizedBox(height: 14),

              // Domain Selector
              const Text(
                'Societal Domain / Sector (Optional - AI will auto-categorize if omitted)',
                style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: AppColors.textPrimary),
              ),
              const SizedBox(height: 6),
              DropdownButtonFormField<String>(
                initialValue: _selectedDomainCode,
                decoration: const InputDecoration(
                  hintText: 'Select domain or leave for AI classification',
                ),
                items: [
                  const DropdownMenuItem(
                    value: null,
                    child: Text('🤖 Auto AI Categorization'),
                  ),
                  ...domainProvider.domains.map(
                    (d) => DropdownMenuItem(
                      value: d.code,
                      child: Text(d.name),
                    ),
                  ),
                ],
                onChanged: (val) {
                  setState(() {
                    _selectedDomainCode = val;
                  });
                },
              ),
              const SizedBox(height: 24),

              // Section 2: Location & GIS
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    '2. Location & GIS Coordinates',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  _isFetchingLocation
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : TextButton.icon(
                          onPressed: _captureGpsLocation,
                          icon: const Icon(Icons.my_location_rounded, size: 16),
                          label: const Text('Capture Live GPS', style: TextStyle(fontSize: 12)),
                        ),
                ],
              ),
              const SizedBox(height: 8),

              Row(
                children: [
                  Expanded(
                    child: CustomTextField(
                      controller: _districtController,
                      label: 'District',
                      validator: (v) => Validators.requiredField(v, fieldName: 'District'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: CustomTextField(
                      controller: _stateController,
                      label: 'State',
                      validator: (v) => Validators.requiredField(v, fieldName: 'State'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              Row(
                children: [
                  Expanded(
                    child: CustomTextField(
                      controller: _pincodeController,
                      label: 'Postal Pincode',
                      keyboardType: TextInputType.number,
                      validator: Validators.pincode,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Jurisdiction Level',
                          style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: AppColors.textPrimary),
                        ),
                        const SizedBox(height: 8),
                        DropdownButtonFormField<String>(
                          initialValue: _selectedJurisdiction,
                          items: const [
                            DropdownMenuItem(value: 'PANCHAYAT_PRI', child: Text('Gram Panchayat')),
                            DropdownMenuItem(value: 'MUNICIPALITY_ULB', child: Text('Municipality (ULB)')),
                            DropdownMenuItem(value: 'DISTRICT', child: Text('District Level')),
                            DropdownMenuItem(value: 'STATE', child: Text('State Level')),
                          ],
                          onChanged: (val) {
                            if (val != null) setState(() => _selectedJurisdiction = val);
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              CustomTextField(
                controller: _addressController,
                label: 'Street / Landmark Address',
                hintText: 'e.g. Near Primary Health Center, Chiraigaon Main Road',
              ),
              const SizedBox(height: 24),

              // Section 3: Citizen Severity/Urgency Assessment
              const Text(
                '3. Citizen Assessment & Affected Population',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 4),
              const Text(
                'Note: This is your ground assessment. Final platform priority is computed by the SamadhanX priority & duplicate scoring engine.',
                style: TextStyle(fontSize: 12, color: AppColors.textSecondary),
              ),
              const SizedBox(height: 12),

              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Citizen Severity', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 6),
                        DropdownButtonFormField<String>(
                          initialValue: _selectedSeverity,
                          items: const [
                            DropdownMenuItem(value: 'LOW', child: Text('Low')),
                            DropdownMenuItem(value: 'MEDIUM', child: Text('Medium')),
                            DropdownMenuItem(value: 'HIGH', child: Text('High')),
                            DropdownMenuItem(value: 'CRITICAL', child: Text('Critical')),
                          ],
                          onChanged: (v) => setState(() => _selectedSeverity = v!),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Citizen Urgency', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 6),
                        DropdownButtonFormField<String>(
                          initialValue: _selectedUrgency,
                          items: const [
                            DropdownMenuItem(value: 'NORMAL', child: Text('Normal')),
                            DropdownMenuItem(value: 'URGENT', child: Text('Urgent')),
                            DropdownMenuItem(value: 'IMMEDIATE', child: Text('Immediate')),
                          ],
                          onChanged: (v) => setState(() => _selectedUrgency = v!),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              CustomTextField(
                controller: _populationController,
                label: 'Estimated Affected People',
                hintText: 'e.g. 1200',
                keyboardType: TextInputType.number,
                validator: (v) => Validators.positiveNumber(v, fieldName: 'Affected population'),
              ),
              const SizedBox(height: 24),

              // Section 4: Evidence Attachments
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    '4. Evidence & Multimedia Proof',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  _isUploadingMedia
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : OutlinedButton.icon(
                          onPressed: _showMediaPickerSheet,
                          icon: const Icon(Icons.attach_file_rounded, size: 16),
                          label: const Text('Add Proof / Photo', style: TextStyle(fontSize: 12)),
                          style: OutlinedButton.styleFrom(
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                            minimumSize: const Size(0, 36),
                          ),
                        ),
                ],
              ),
              const SizedBox(height: 8),

              if (_attachments.isEmpty)
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppColors.divider),
                  ),
                  child: const Center(
                    child: Column(
                      children: [
                        Icon(Icons.cloud_upload_outlined, size: 32, color: AppColors.textMuted),
                        SizedBox(height: 6),
                        Text(
                          'Upload photo evidence from camera/gallery or attach lab test reports to accelerate departmental triage.',
                          style: TextStyle(fontSize: 12, color: AppColors.textSecondary),
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                  ),
                )
              else
                ListView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: _attachments.length,
                  itemBuilder: (context, idx) {
                    final att = _attachments[idx];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        leading: Icon(
                          att.mediaType == 'IMAGE'
                              ? Icons.image_outlined
                              : att.mediaType == 'DOCUMENT'
                                  ? Icons.description_outlined
                                  : Icons.video_collection_outlined,
                          color: AppColors.primary,
                        ),
                        title: Text(att.fileName, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold)),
                        subtitle: Text(att.caption ?? att.mediaType, style: const TextStyle(fontSize: 11)),
                        trailing: IconButton(
                          icon: const Icon(Icons.delete_outline_rounded, color: AppColors.ruby, size: 20),
                          onPressed: () => setState(() => _attachments.removeAt(idx)),
                        ),
                      ),
                    );
                  },
                ),
              const SizedBox(height: 32),

              // Submit Button
              CustomButton(
                text: 'Submit Societal Challenge',
                icon: Icons.send_rounded,
                isLoading: challengeProvider.isSubmitting,
                onPressed: _handleSubmit,
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}
