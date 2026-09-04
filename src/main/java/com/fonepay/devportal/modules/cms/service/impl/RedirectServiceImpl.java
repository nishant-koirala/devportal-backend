package com.fonepay.devportal.modules.cms.service.impl;

import org.springframework.stereotype.Service;
import com.fonepay.devportal.modules.cms.service.RedirectService;
import com.fonepay.devportal.modules.cms.repository.RedirectRepository;
import com.fonepay.devportal.modules.cms.document.Redirect;
import com.fonepay.devportal.common.util.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.Clock;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedirectServiceImpl implements RedirectService {

    private final RedirectRepository redirectRepository;
    private final Clock clock;

    @Override
    public void createRedirect(String productId, String pageId, String oldPath, String newPath) {
        if (oldPath == null || newPath == null || oldPath.equals(newPath)) {
            return; // No redirect needed if paths are the same
        }

        // Delete any existing redirect that had this oldPath to avoid unique constraint violations
        redirectRepository.deleteByOldPath(oldPath);

        // Also check if the oldPath is currently a newPath of an existing redirect and update it (redirect chaining)
        // For simplicity, we just create a new redirect from oldPath -> newPath
        Redirect redirect = Redirect.builder()
                .id(IdGenerator.nextUlid())
                .productId(productId)
                .pageId(pageId)
                .oldPath(oldPath)
                .newPath(newPath)
                .createdAt(clock.instant())
                .build();
        
        redirectRepository.save(redirect);
        log.info("Created redirect from {} to {}", oldPath, newPath);
    }

    @Override
    public String resolveRedirect(String path) {
        Optional<Redirect> redirectOpt = redirectRepository.findByOldPath(path);
        
        if (redirectOpt.isPresent()) {
            String newPath = redirectOpt.get().getNewPath();
            // Resolve transitively if needed (simple depth limit to prevent cycles)
            int depth = 0;
            while (depth < 5) {
                Optional<Redirect> nextOpt = redirectRepository.findByOldPath(newPath);
                if (nextOpt.isPresent()) {
                    newPath = nextOpt.get().getNewPath();
                    depth++;
                } else {
                    break;
                }
            }
            return newPath;
        }
        return null;
    }
}
