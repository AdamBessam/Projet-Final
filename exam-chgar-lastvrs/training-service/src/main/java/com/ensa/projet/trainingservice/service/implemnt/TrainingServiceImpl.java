package com.ensa.projet.trainingservice.service.implemnt;

import com.ensa.projet.trainingservice.exception.ResourceNotFoundException;
import com.ensa.projet.trainingservice.model.dao.ModuleDTO;
import com.ensa.projet.trainingservice.model.dao.TrainingDTO;
import com.ensa.projet.trainingservice.model.entities.Category;
import com.ensa.projet.trainingservice.model.entities.Training;
import com.ensa.projet.trainingservice.model.entities.Module;
import com.ensa.projet.trainingservice.repository.CategoryRepository;
import com.ensa.projet.trainingservice.repository.ModuleRepository;
import com.ensa.projet.trainingservice.repository.TrainingRepository;
import com.ensa.projet.trainingservice.service.interfaces.TrainingService;
import com.ensa.projet.trainingservice.service.FileStorageService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final CategoryRepository categoryRepository;
    private final ModuleRepository moduleRepository;
    private final FileStorageService fileStorageService;
    private static final String GENERIC_ERROR_MESSAGE = "Training not found";

    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            CategoryRepository categoryRepository,
            ModuleRepository moduleRepository,
            FileStorageService fileStorageService) {
        this.trainingRepository = trainingRepository;
        this.categoryRepository = categoryRepository;
        this.moduleRepository = moduleRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public TrainingDTO createTraining(TrainingDTO trainingDTO) {
        Category category = categoryRepository.findById(trainingDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Training training = new Training();
        training.setTitle(trainingDTO.getTitle());
        training.setDescription(trainingDTO.getDescription());
        training.setIconPath(trainingDTO.getIconPath());
        training.setDifficultyLevel(trainingDTO.getDifficultyLevel());
        training.setEstimatedDurationMinutes(trainingDTO.getEstimatedDurationMinutes());
        training.setGoals(trainingDTO.getGoals());
        training.setPrerequisites(trainingDTO.getPrerequisites());
        training.setUrlYtb(trainingDTO.getUrlYtb());
        training.setSupportAR(trainingDTO.isSupportAR());
        training.setSupportAI(trainingDTO.isSupportAI());
        training.setCategory(category);
        training.setInstructions(trainingDTO.getInstructions());
        training.setPublished(false);

        Training saved = trainingRepository.save(training);
        return convertToDTO(saved);
    }

    @Override
    public TrainingDTO updateTraining(Integer id, TrainingDTO trainingDTO) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));

        // Gestion de l'icône : supprimer l'ancienne si nouvelle icône
        if (trainingDTO.getIconPath() != null && !trainingDTO.getIconPath().equals(training.getIconPath())) {
            if (training.getIconPath() != null) {
                fileStorageService.deleteFile(training.getIconPath());
            }
        }

        // Gestion de la catégorie
        if (!training.getCategory().getId().equals(trainingDTO.getCategoryId())) {
            Category category = categoryRepository.findById(trainingDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            training.setCategory(category);
        }

        // Mise à jour des champs
        training.setTitle(trainingDTO.getTitle());
        training.setDescription(trainingDTO.getDescription());
        training.setIconPath(trainingDTO.getIconPath());
        training.setDifficultyLevel(trainingDTO.getDifficultyLevel());
        training.setEstimatedDurationMinutes(trainingDTO.getEstimatedDurationMinutes());
        training.setGoals(trainingDTO.getGoals());
        training.setPrerequisites(trainingDTO.getPrerequisites());
        training.setUrlYtb(trainingDTO.getUrlYtb());
        training.setSupportAR(trainingDTO.isSupportAR());
        training.setSupportAI(trainingDTO.isSupportAI());
        training.setInstructions(trainingDTO.getInstructions());

        Training updated = trainingRepository.save(training);
        return convertToDTO(updated);
    }

    @Override
    public void deleteTraining(Integer id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));

        // Supprimer l'icône si elle existe
        if (training.getIconPath() != null) {
            fileStorageService.deleteFile(training.getIconPath());
        }

        trainingRepository.deleteById(id);
    }

    @Override
    public TrainingDTO getTrainingById(Integer id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));
        return convertToDTO(training);
    }

    @Override
    public List<TrainingDTO> getAllTrainings() {
        List<Training> trainings = trainingRepository.findAll();
        List<TrainingDTO> trainingDTOs = new ArrayList<>();
        for (Training training : trainings) {
            trainingDTOs.add(convertToDTO(training));
        }
        return trainingDTOs;
    }

    @Override
    public List<TrainingDTO> getTrainingsByCategory(Integer categoryId) {
        List<Training> trainings = trainingRepository.findByCategoryId(categoryId);
        List<TrainingDTO> trainingDTOs = new ArrayList<>();
        for (Training training : trainings) {
            trainingDTOs.add(convertToDTO(training));
        }
        return trainingDTOs;
    }

    @Override
    public ModuleDTO addModule(Integer trainingId, ModuleDTO moduleDTO) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));

        Module module = new Module();
        module.setTitle(moduleDTO.getTitle());
        module.setDescription(moduleDTO.getDescription());
        module.setOrderIndex(moduleDTO.getOrderIndex());
        module.setTraining(training);

        Module saved = moduleRepository.save(module);
        return convertToModuleDTO(saved);
    }

    @Override
    public void publishTraining(Integer id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));
        training.setPublished(true);
        trainingRepository.save(training);
    }

    @Override
    public void unpublishTraining(Integer id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));
        training.setPublished(false);
        trainingRepository.save(training);
    }

    private TrainingDTO convertToDTO(Training training) {
        TrainingDTO dto = new TrainingDTO();
        dto.setId(training.getId());
        dto.setTitle(training.getTitle());
        dto.setDescription(training.getDescription());
        dto.setIconPath(training.getIconPath());
        dto.setDifficultyLevel(training.getDifficultyLevel());
        dto.setEstimatedDurationMinutes(training.getEstimatedDurationMinutes());
        dto.setGoals(training.getGoals());
        dto.setPrerequisites(training.getPrerequisites());
        dto.setUrlYtb(training.getUrlYtb());
        dto.setSupportAR(training.isSupportAR());
        dto.setSupportAI(training.isSupportAI());
        dto.setCategoryId(training.getCategory().getId());
        dto.setCategoryName(training.getCategory().getName());
        dto.setInstructions(training.getInstructions());
        dto.setCreatedAt(training.getCreatedAt());
        dto.setUpdatedAt(training.getUpdatedAt());
        dto.setPublished(training.isPublished());

        List<ModuleDTO> moduleDTOs = new ArrayList<>();
        for (Module module : training.getModules()) {
            moduleDTOs.add(convertToModuleDTO(module));
        }
        dto.setModules(moduleDTOs);

        return dto;
    }

    private ModuleDTO convertToModuleDTO(Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setOrderIndex(module.getOrderIndex());
        return dto;
    }
}